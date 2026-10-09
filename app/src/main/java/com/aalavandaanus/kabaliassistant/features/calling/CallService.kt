package com.aalavandaanus.kabaliassistant.features.calling

import android.content.Context
import android.content.Intent
import android.net.Uri

interface CallService {

    fun call(phoneNumber: String): Boolean
}

class AndroidCallService(
    private val context: Context
) : CallService {

    override fun call(phoneNumber: String): Boolean {

        if (phoneNumber.isBlank()) {
            return false
        }

        val intent = Intent(
            Intent.ACTION_DIAL,
            Uri.parse("tel:${Uri.encode(phoneNumber)}")
        ).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (exception: Exception) {
            false
        }
    }
}
