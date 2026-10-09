package com.aalavandaanus.kabaliassistant.features.calling

import android.content.Context
import android.provider.ContactsContract

interface ContactRepository {

    fun getContacts(): List<Contact>
}

class AndroidContactRepository(
    private val context: Context
) : ContactRepository {

    override fun getContacts(): List<Contact> {

        val contactsById = linkedMapOf<Long, MutableContact>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE
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
                it.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                )

            val nameIndex =
                it.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                )

            val numberIndex =
                it.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                )

            val typeIndex =
                it.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.TYPE
                )

            while (it.moveToNext()) {

                val id = it.getLong(idIndex)
                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)
                val type = it.getInt(typeIndex)

                if (name.isNullOrBlank() || number.isNullOrBlank()) {
                    continue
                }

                val phoneNumber = PhoneNumber(
                    number = number,
                    type = mapPhoneNumberType(type)
                )

                val existing = contactsById[id]

                if (existing == null) {

                    contactsById[id] = MutableContact(
                        id = id,
                        displayName = name,
                        phoneNumbers = mutableListOf(phoneNumber)
                    )

                } else {

                    existing.phoneNumbers.add(phoneNumber)
                }
            }
        }

        return contactsById.values.map { mutableContact ->

            Contact(
                id = mutableContact.id,
                displayName = mutableContact.displayName,
                phoneNumbers = mutableContact.phoneNumbers.toList()
            )
        }
    }

    private fun mapPhoneNumberType(
        type: Int
    ): PhoneNumberType {

        return when (type) {

            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ->
                PhoneNumberType.MOBILE

            ContactsContract.CommonDataKinds.Phone.TYPE_HOME ->
                PhoneNumberType.HOME

            ContactsContract.CommonDataKinds.Phone.TYPE_WORK ->
                PhoneNumberType.WORK

            else ->
                PhoneNumberType.OTHER
        }
    }

    private data class MutableContact(
        val id: Long,
        val displayName: String,
        val phoneNumbers: MutableList<PhoneNumber>
    )
}
