package com.aalavandaanus.kabaliassistant.features.calling

sealed interface ContactResolutionResult {

    data class Found(
        val contact: Contact
    ) : ContactResolutionResult

    data class Ambiguous(
        val contacts: List<Contact>
    ) : ContactResolutionResult

    data object NotFound : ContactResolutionResult
}

interface ContactResolver {
    fun resolve(
        spokenName: String,
        contacts: List<Contact>
    ): ContactResolutionResult
}

class DefaultContactResolver : ContactResolver {

    override fun resolve(
        spokenName: String,
        contacts: List<Contact>
    ): ContactResolutionResult {

        val query = normalize(spokenName)

        if (query.isBlank()) {
            return ContactResolutionResult.NotFound
        }

        val exactMatches = contacts.filter {
            normalize(it.displayName) == query
        }

        if (exactMatches.isNotEmpty()) {
            return resolveMatches(exactMatches)
        }

        val queryWords = query.split(" ")

        val matches = contacts.filter { contact ->
            val contactWords = normalize(contact.displayName)
                .split(" ")

            // Every spoken word must exist as a complete word
            // in the contact's display name.
            queryWords.all { it in contactWords }
        }

        return when (matches.size) {
            0 -> ContactResolutionResult.NotFound
            1 -> ContactResolutionResult.Found(matches.first())
            else -> ContactResolutionResult.Ambiguous(matches)
        }
    }

    private fun resolveMatches(
        matches: List<Contact>
    ): ContactResolutionResult {
        return if (matches.size == 1) {
            ContactResolutionResult.Found(matches.first())
        } else {
            ContactResolutionResult.Ambiguous(matches)
        }
    }

    private fun normalize(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
    }
}
