package com.aalavandaanus.kabaliassistant.features.familytree

import java.time.LocalDate

data class FamilyPerson(
    val id: String,
    val fullName: String,
    val preferredName: String? = null,
    val birthDate: LocalDate? = null,
    val deathDate: LocalDate? = null,
    val birthplace: String? = null,
    val note: String? = null,
    val photoAsset: String? = null,
    val generation: Int,
    val parentIds: List<String> = emptyList(),
    val spouseIds: List<String> = emptyList(),
    val childIds: List<String> = emptyList(),
    val otherRelativeIds: List<String> = emptyList()
) {
    val displayName: String get() = preferredName?.takeIf(String::isNotBlank) ?: fullName
}
