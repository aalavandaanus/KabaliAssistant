package com.aalavandaanus.kabaliassistant.features.calling

import android.content.Context
import android.provider.ContactsContract

class ContactRepository(
    private val context: Context
) {

    fun getContacts(): List<Contact> {

        val contacts = mutableListOf<Contact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        ) ?: return emptyList()

        cursor.use {

            val idIndex =
                it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

            val nameIndex =
                it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)

            val numberIndex =
                it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {

                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val phoneNumber = it.getString(numberIndex)

                if (!name.isNullOrBlank() && !phoneNumber.isNullOrBlank()) {
                    contacts.add(
                        Contact(
                            id = id,
                            displayName = name,
                            phoneNumber = phoneNumber
                        )
                    )
                }
            }
        }

        return contacts
    }
}