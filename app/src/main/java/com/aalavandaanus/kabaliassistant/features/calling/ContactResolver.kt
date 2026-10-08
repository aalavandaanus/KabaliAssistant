package com.aalavandaanus.kabaliassistant.features.calling

data class Contact(
    val id: Long,
    val displayName: String,
    val phoneNumber: String
)

sealed interface ContactResolutionResult {

    data class Found(
        val contact: Contact
    ) : ContactResolutionResult

    data class Ambiguous(
        val contacts: List<Contact>
    ) : ContactResolutionResult

    data object NotFound : ContactResolutionResult
}

class ContactResolver {

    fun resolve(
        spokenName: String,
        contacts: List<Contact>
    ): ContactResolutionResult {

        val query = normalize(spokenName)

        if (query.isBlank()) {
            return ContactResolutionResult.NotFound
        }

        /*
         * 1. Exact full-name match.
         *
         * Example:
         * "ACC Muthu" -> "ACC Muthu"
         */
        val exactMatches = contacts.filter {
            normalize(it.displayName) == query
        }

        if (exactMatches.size == 1) {
            return ContactResolutionResult.Found(exactMatches.first())
        }

        if (exactMatches.size > 1) {
            return ContactResolutionResult.Ambiguous(exactMatches)
        }

        /*
         * 2. Match the person's name after a context prefix.
         *
         * Example:
         * "Muthu" -> "ACC Muthu"
         * "Santhosh" -> "REL Santhosh"
         */
        val suffixMatches = contacts.filter {
            val name = normalize(it.displayName)

            name.endsWith(" $query")
        }

        if (suffixMatches.size == 1) {
            return ContactResolutionResult.Found(suffixMatches.first())
        }

        if (suffixMatches.size > 1) {
            return ContactResolutionResult.Ambiguous(suffixMatches)
        }

        return ContactResolutionResult.NotFound
    }

    private fun normalize(value: String): String {
        return value
            .trim()
            .replace(Regex("\\s+"), " ")
            .lowercase()
    }
}