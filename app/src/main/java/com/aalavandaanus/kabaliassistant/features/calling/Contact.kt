package com.aalavandaanus.kabaliassistant.features.calling

data class Contact(
    val id: Long,
    val displayName: String,
    val phoneNumbers: List<PhoneNumber>
)

data class PhoneNumber(
    val number: String,
    val type: PhoneNumberType
)

enum class PhoneNumberType {
    MOBILE,
    HOME,
    WORK,
    OTHER
}