package com.aalavandaanus.kabaliassistant.features.familytree

import android.content.Context

/** Device-local identity choice; unrelated to Google/Firebase sign-in. */
class FamilyIdentityStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kabali_family_tree", Context.MODE_PRIVATE)
    fun getPersonId(): String? = prefs.getString(KEY_PERSON_ID, null)
    fun setPersonId(id: String) { prefs.edit().putString(KEY_PERSON_ID, id).apply() }
    fun clear() { prefs.edit().remove(KEY_PERSON_ID).apply() }
    private companion object { const val KEY_PERSON_ID = "identified_person_id" }
}
