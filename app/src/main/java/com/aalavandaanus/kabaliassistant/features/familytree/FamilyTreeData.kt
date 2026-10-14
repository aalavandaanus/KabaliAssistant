package com.aalavandaanus.kabaliassistant.features.familytree

import java.time.LocalDate

/** Replace these explicitly fictional preview records with the authoritative family dataset. */
object FamilyTreeData {
    const val IS_PREVIEW_DATA = false

    val people = listOf(
        FamilyPerson("Thatha", "Kanniappan", birthDate = LocalDate.of(1940, 3, 12), generation = 0, childIds = listOf("Murugesan","Rajasekar")),
        FamilyPerson("Paatti", "Maaraayaal", birthDate = LocalDate.of(1944, 7, 8), generation = 0, childIds = listOf("Murugesan","Rajasekar")),

        FamilyPerson("Murugesan", "K.K.Murugesan", birthDate = LocalDate.of(1967, 10, 16), generation = 1, parentIds = listOf("Thatha", "Paatti"), spouseIds = listOf("Palaniammal"), childIds = listOf("Ravishankar", "Sivashankar","Sivashankari")),
        FamilyPerson("Palaniammal", "K.M.Palaniammal", birthDate = LocalDate.of(1970, 2, 19), generation = 1, spouseIds = listOf("Murugesan"), childIds = listOf("Ravishankar", "Sivashankar","Sivashankari")),


        FamilyPerson("Ravishankar", "K.M.Ravishankar", preferredName = "Ravishankar", birthDate = LocalDate.of(1984, 9, 21), generation = 2, parentIds = listOf("Murugesan", "Palaniammal"), spouseIds = listOf("Vishalakshi"), childIds = listOf("Vyshya","Leka","Suthiksha")),
        FamilyPerson("Vishalakshi", "G.Vishalakshi", birthDate = LocalDate.of(1986, 12, 15), generation = 2,  spouseIds = listOf("Ravishankar"), childIds = listOf("Vyshya","Leka","Suthiksha")),

        FamilyPerson("Vyshya", "R.Vyshya", birthDate = LocalDate.of(2012, 6, 30), generation = 3, parentIds = listOf("Ravishankar", "Vishalakshi")),
        FamilyPerson("Leka", "R.Lekashri", birthDate = LocalDate.of(2012, 6, 30), generation = 3, parentIds = listOf("Ravishankar", "Vishalakshi")),
        FamilyPerson("Suthiksha", "R.Suthiksha", birthDate = LocalDate.of(2012, 6, 30), generation = 3, parentIds = listOf("Ravishankar", "Vishalakshi")),

        FamilyPerson("Sivashankar", "K.M.Sivashankar", birthDate = LocalDate.of(1988, 5, 2), generation = 2, parentIds = listOf("Murugesan", "Palaniammal"), childIds = listOf("Mithran")),
        FamilyPerson("Sugirtha", "Sugirtha", birthDate = LocalDate.of(1986, 12, 15), generation = 2, spouseIds = listOf("Sivashankar"), childIds = listOf("Mithran")),

        FamilyPerson("Mithran", "Mayur Mithran", birthDate = LocalDate.of(2012, 6, 30), generation = 3, parentIds = listOf("Sivashankar", "Sugirtha")),

        FamilyPerson("Sivashankari", "K.M.Sivashankari", birthDate = LocalDate.of(1988, 5, 2), generation = 2, spouseIds = listOf("Santhosh"),parentIds = listOf("Murugesan", "Palaniammal"), childIds = listOf("ABHIRATHI")),
        FamilyPerson("Santhosh", "Santhosh", birthDate = LocalDate.of(1988, 5, 2), generation = 2, spouseIds = listOf("Sivashankari"),childIds = listOf("ABHIRATHI")),

        FamilyPerson("ABHIRATHI", "SAI ABHIRATHI", birthDate = LocalDate.of(2012, 10, 13), generation = 3, parentIds = listOf("Sivashankari", "Santhosh")),

        FamilyPerson("Rajasekar", "K.K.Rajasekar", birthDate = LocalDate.of(1967, 11, 4), generation = 1, parentIds = listOf("Thatha", "Paatti"), spouseIds = listOf("Pongiammal"), childIds = listOf("Vanathi", "Nalini")),
        FamilyPerson("Pongiammal", "K.R.Pongiammal", birthDate = LocalDate.of(1967, 11, 4), generation = 1, parentIds = listOf("Chellamuthu", "Aaya"), spouseIds = listOf("Rajasekar"), childIds = listOf("Vanathi", "Nalini")),

        FamilyPerson("Vanathi", "K.R.Vanathi", preferredName = "Vanathi", birthDate = LocalDate.of(1984, 9, 21), generation = 2, parentIds = listOf("Rajasekar", "Pongiammal")),
        FamilyPerson("Nalini", "K.R.Nalini", preferredName = "Nalini", birthDate = LocalDate.of(1984, 9, 21), generation = 2, parentIds = listOf("Rajasekar", "Pongiammal")),


    )

    private val byId by lazy { people.associateBy(FamilyPerson::id) }
    fun person(id: String?): FamilyPerson? = id?.let(byId::get)
    fun parents(person: FamilyPerson) = person.parentIds.mapNotNull(byId::get)
    fun spouses(person: FamilyPerson) = person.spouseIds.mapNotNull(byId::get)
    fun children(person: FamilyPerson) = person.childIds.mapNotNull(byId::get)
    fun otherRelatives(person: FamilyPerson) = person.otherRelativeIds.mapNotNull(byId::get)

    fun relationshipLabel(from: FamilyPerson?, to: FamilyPerson): String {
        if (from == null || from.id == to.id) return "Family member"
        return when {
            to.id in from.parentIds -> "Child"
            to.id in from.childIds -> "Parent"
            to.id in from.spouseIds -> "Spouse"
            to.id in from.otherRelativeIds -> "Relative"
            from.id in to.parentIds -> "Parent"
            from.id in to.childIds -> "Child"
            from.id in to.spouseIds -> "Spouse"
            else -> "Family member"
        }
    }
}
