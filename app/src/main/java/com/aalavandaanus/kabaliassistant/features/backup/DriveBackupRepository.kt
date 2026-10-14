package com.aalavandaanus.kabaliassistant.features.backup

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Base64
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Metadata for one backup file stored in the user's Google Drive. */
data class DriveBackupFile(
    val id: String,
    val name: String,
    val createdTime: String
)

/**
 * Creates and restores a versioned JSON snapshot of Kabali's Room tables and
 * user-facing SharedPreferences. The caller must supply a valid Google Drive
 * OAuth access token obtained through the explicit authorization flow.
 */
class DriveBackupRepository(
    private val context: Context,
    private val roomDatabase: RoomDatabase
) {
    companion object {
        private const val DRIVE_API = "https://www.googleapis.com/drive/v3"
        private const val DRIVE_UPLOAD_API = "https://www.googleapis.com/upload/drive/v3"
        private const val FOLDER_NAME = "Kabali Backups"
        private const val BACKUP_PREFIX = "kabali-backup-"
        private const val BACKUP_FORMAT = "kabali-backup"
        private const val BACKUP_VERSION = 1
        private const val MIME_JSON = "application/json"
        private const val MIME_FOLDER = "application/vnd.google-apps.folder"
        private const val MAX_BACKUPS = 3

        // These are the real Room table names in KabaliDatabase version 5.
        private val TABLES = listOf(
            "alarms",
            "reminders",
            "birthdays",
            "schedule_categories",
            "schedules"
        )

        // Delete child rows before parent rows to respect schedule foreign keys.
        private val DELETE_ORDER = listOf(
            "schedules",
            "schedule_categories",
            "alarms",
            "birthdays",
            "reminders"
        )

        private val INSERT_ORDER = listOf(
            "schedule_categories",
            "alarms",
            "birthdays",
            "reminders",
            "schedules"
        )
    }

    suspend fun createBackup(accessToken: String): List<DriveBackupFile> =
        withContext(Dispatchers.IO) {
            val payload = buildBundle()
            val folderId = findOrCreateFolder(accessToken)
            val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
            val fileName = "$BACKUP_PREFIX$stamp.json"
            val newFileId = uploadJson(accessToken, folderId, fileName, payload)

            // Never prune older files until the new upload can be downloaded and validated.
            validateBundle(downloadJson(accessToken, newFileId))

            val current = listBackupsInternal(accessToken, folderId)
                .sortedWith(
                    compareByDescending<DriveBackupFile> { it.id == newFileId }
                        .thenByDescending { it.createdTime }
                )
            current.drop(MAX_BACKUPS).forEach { old ->
                driveRequest(
                    accessToken = accessToken,
                    method = "DELETE",
                    url = "$DRIVE_API/files/${urlPath(old.id)}"
                )
            }

            listBackupsInternal(accessToken, folderId)
                .sortedByDescending { it.createdTime }
        }

    suspend fun listBackups(accessToken: String): List<DriveBackupFile> =
        withContext(Dispatchers.IO) {
            val folderId = findFolder(accessToken) ?: return@withContext emptyList()
            listBackupsInternal(accessToken, folderId)
                .sortedByDescending { it.createdTime }
        }

    suspend fun restoreBackup(accessToken: String, fileId: String) =
        withContext(Dispatchers.IO) {
            val bundle = downloadJson(accessToken, fileId)
            validateBundle(bundle)
            restoreBundle(bundle)
        }

    private fun buildBundle(): JSONObject {
        val database = roomDatabase.openHelper.writableDatabase
        val tablesJson = JSONObject()
        TABLES.forEach { table -> tablesJson.put(table, readTable(database, table)) }

        val preferencesJson = JSONObject()
            .put("kabali_profile", readPreferences("kabali_profile"))
            .put("kabali_assistant_settings", readPreferences("kabali_assistant_settings"))

        return JSONObject()
            .put("format", BACKUP_FORMAT)
            .put("version", BACKUP_VERSION)
            .put("createdAt", System.currentTimeMillis())
            .put("database", tablesJson)
            .put("preferences", preferencesJson)
    }

    private fun readTable(database: SupportSQLiteDatabase, table: String): JSONArray {
        val rows = JSONArray()
        database.query("SELECT * FROM `$table`").use { cursor ->
            while (cursor.moveToNext()) {
                val row = JSONObject()
                for (index in 0 until cursor.columnCount) {
                    val column = cursor.getColumnName(index)
                    when (cursor.getType(index)) {
                        Cursor.FIELD_TYPE_NULL -> row.put(column, JSONObject.NULL)
                        Cursor.FIELD_TYPE_INTEGER -> row.put(column, cursor.getLong(index))
                        Cursor.FIELD_TYPE_FLOAT -> row.put(column, cursor.getDouble(index))
                        Cursor.FIELD_TYPE_BLOB -> row.put(
                            column,
                            JSONObject().put(
                                "__kabali_blob_base64",
                                Base64.encodeToString(cursor.getBlob(index), Base64.NO_WRAP)
                            )
                        )
                        else -> row.put(column, cursor.getString(index))
                    }
                }
                rows.put(row)
            }
        }
        return rows
    }

    private fun readPreferences(name: String): JSONObject {
        val result = JSONObject()
        context.getSharedPreferences(name, Context.MODE_PRIVATE).all.forEach { (key, value) ->
            when (value) {
                null -> result.put(key, JSONObject.NULL)
                is Set<*> -> result.put(key, JSONArray(value.map { it.toString() }))
                is Boolean, is Int, is Long, is Float, is String -> result.put(key, value)
                else -> result.put(key, value.toString())
            }
        }
        return result
    }

    private fun validateBundle(bundle: JSONObject) {
        require(bundle.optString("format") == BACKUP_FORMAT) {
            "This is not a Kabali backup file."
        }
        require(bundle.optInt("version", -1) == BACKUP_VERSION) {
            "This backup version is not supported by this version of Kabali."
        }
        val database = bundle.optJSONObject("database")
            ?: error("Backup is missing its database section.")
        TABLES.forEach { table ->
            require(database.optJSONArray(table) != null) {
                "Backup is missing the $table data."
            }
        }
        val preferences = bundle.optJSONObject("preferences")
            ?: error("Backup is missing profile/settings preferences.")
        require(preferences.optJSONObject("kabali_profile") != null) {
            "Backup is missing profile preferences."
        }
        require(preferences.optJSONObject("kabali_assistant_settings") != null) {
            "Backup is missing Kabali settings."
        }
    }

    private fun restoreBundle(bundle: JSONObject) {
        val database = roomDatabase.openHelper.writableDatabase
        val tables = bundle.getJSONObject("database")

        // Validate that all table names and columns are compatible before deleting data.
        val allowedColumns = TABLES.associateWith { tableColumns(database, it) }
        TABLES.forEach { table ->
            val rows = tables.getJSONArray(table)
            for (index in 0 until rows.length()) {
                val row = rows.getJSONObject(index)
                val keys = row.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    require(key in allowedColumns.getValue(table)) {
                        "This backup contains an unsupported column '$key' in '$table'."
                    }
                }
            }
        }

        database.beginTransaction()
        try {
            DELETE_ORDER.forEach { table -> database.delete(table, null, null) }

            INSERT_ORDER.forEach { table ->
                val rows = tables.getJSONArray(table)
                for (index in 0 until rows.length()) {
                    val row = rows.getJSONObject(index)
                    val values = ContentValues()
                    val keys = row.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = row.get(key)
                        when {
                            value == JSONObject.NULL -> values.putNull(key)
                            value is JSONObject && value.has("__kabali_blob_base64") ->
                                values.put(
                                    key,
                                    Base64.decode(
                                        value.getString("__kabali_blob_base64"),
                                        Base64.DEFAULT
                                    )
                                )
                            value is String -> values.put(key, value)
                            value is Boolean -> values.put(key, if (value) 1 else 0)
                            value is Int -> values.put(key, value)
                            value is Long -> values.put(key, value)
                            value is Double -> values.put(key, value)
                            else -> values.put(key, value.toString())
                        }
                    }
                    val inserted = database.insert(table, SQLiteDatabase.CONFLICT_ABORT, values)
                    check(inserted != -1L) { "Could not restore a row in '$table'." }
                }
            }
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }

        // Preferences are applied only after the database transaction succeeds.
        val preferences = bundle.getJSONObject("preferences")
        restorePreferences("kabali_profile", preferences.getJSONObject("kabali_profile"))
        restorePreferences(
            "kabali_assistant_settings",
            preferences.getJSONObject("kabali_assistant_settings")
        )
    }

    private fun tableColumns(database: SupportSQLiteDatabase, table: String): Set<String> {
        val result = mutableSetOf<String>()
        database.query("PRAGMA table_info(`$table`)").use { cursor ->
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex >= 0) result += cursor.getString(nameIndex)
            }
        }
        require(result.isNotEmpty()) { "The local database is missing table '$table'." }
        return result
    }

    private fun restorePreferences(name: String, json: JSONObject) {
        val editor = context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear()
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.get(key)
            when {
                value == JSONObject.NULL -> Unit
                value is String -> editor.putString(key, value)
                value is Boolean -> editor.putBoolean(key, value)
                value is Int -> editor.putInt(key, value)
                value is Long -> editor.putLong(key, value)
                value is Double -> editor.putFloat(key, value.toFloat())
                value is JSONArray -> {
                    val values = mutableSetOf<String>()
                    for (index in 0 until value.length()) values += value.getString(index)
                    editor.putStringSet(key, values)
                }
                else -> editor.putString(key, value.toString())
            }
        }
        check(editor.commit()) { "Could not save restored preferences for '$name'." }
    }

    private fun findOrCreateFolder(accessToken: String): String {
        findFolder(accessToken)?.let { return it }
        val metadata = JSONObject().put("name", FOLDER_NAME).put("mimeType", MIME_FOLDER)
        val response = driveRequest(
            accessToken = accessToken,
            method = "POST",
            url = "$DRIVE_API/files?fields=id,name",
            body = metadata.toString().toByteArray(StandardCharsets.UTF_8),
            contentType = "application/json; charset=UTF-8"
        )
        return JSONObject(response).getString("id")
    }

    private fun findFolder(accessToken: String): String? {
        val query = "name = '$FOLDER_NAME' and mimeType = '$MIME_FOLDER' and trashed = false"
        val url = "$DRIVE_API/files?q=${urlQuery(query)}&pageSize=100&fields=files(id,name,mimeType)"
        val files = JSONObject(driveRequest(accessToken, "GET", url)).optJSONArray("files")
            ?: return null
        if (files.length() == 0) return null
        return files.getJSONObject(0).getString("id")
    }

    private fun listBackupsInternal(
        accessToken: String,
        folderId: String
    ): List<DriveBackupFile> {
        val query = "'$folderId' in parents and trashed = false"
        val url = "$DRIVE_API/files?q=${urlQuery(query)}&pageSize=100&orderBy=createdTime%20desc&fields=files(id,name,createdTime)"
        val files = JSONObject(driveRequest(accessToken, "GET", url)).optJSONArray("files")
            ?: JSONArray()
        val result = mutableListOf<DriveBackupFile>()
        for (index in 0 until files.length()) {
            val file = files.getJSONObject(index)
            val name = file.optString("name")
            if (name.startsWith(BACKUP_PREFIX) && name.endsWith(".json")) {
                result += DriveBackupFile(
                    id = file.getString("id"),
                    name = name,
                    createdTime = file.optString("createdTime")
                )
            }
        }
        return result
    }

    private fun uploadJson(
        accessToken: String,
        folderId: String,
        fileName: String,
        payload: JSONObject
    ): String {
        val boundary = "kabali_${System.currentTimeMillis()}"
        val metadata = JSONObject()
            .put("name", fileName)
            .put("mimeType", MIME_JSON)
            .put("parents", JSONArray().put(folderId))
        val output = java.io.ByteArrayOutputStream()
        output.write("--$boundary\r\n".toByteArray())
        output.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray())
        output.write(metadata.toString().toByteArray(StandardCharsets.UTF_8))
        output.write("\r\n--$boundary\r\n".toByteArray())
        output.write("Content-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray())
        output.write(payload.toString().toByteArray(StandardCharsets.UTF_8))
        output.write("\r\n--$boundary--\r\n".toByteArray())

        val response = driveRequest(
            accessToken = accessToken,
            method = "POST",
            url = "$DRIVE_UPLOAD_API/files?uploadType=multipart&fields=id,name,createdTime",
            body = output.toByteArray(),
            contentType = "multipart/related; boundary=$boundary"
        )
        return JSONObject(response).getString("id")
    }

    private fun downloadJson(accessToken: String, fileId: String): JSONObject {
        val response = driveRequest(
            accessToken = accessToken,
            method = "GET",
            url = "$DRIVE_API/files/${urlPath(fileId)}?alt=media"
        )
        return JSONObject(response)
    }

    private fun driveRequest(
        accessToken: String,
        method: String,
        url: String,
        body: ByteArray? = null,
        contentType: String? = null
    ): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 20_000
            connection.readTimeout = 30_000
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", contentType ?: "application/octet-stream")
                connection.outputStream.use { it.write(body) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.use {
                BufferedReader(InputStreamReader(it, StandardCharsets.UTF_8)).readText()
            }.orEmpty()
            if (code !in 200..299) {
                val detail = try {
                    JSONObject(response).optJSONObject("error")?.optString("message")
                } catch (_: Exception) {
                    null
                }
                throw IllegalStateException(
                    detail?.takeIf { it.isNotBlank() }
                        ?: "Google Drive request failed (HTTP $code)."
                )
            }
            return response
        } finally {
            connection.disconnect()
        }
    }

    private fun urlQuery(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())

    private fun urlPath(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8.name())
}
