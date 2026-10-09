package com.aalavandaanus.kabaliassistant.core.commands

import com.aalavandaanus.kabaliassistant.features.calling.CallCommandHandler
import com.aalavandaanus.kabaliassistant.features.calling.CallCommandHandlerResult
import com.aalavandaanus.kabaliassistant.features.calling.Contact

class CommandRouter(
    private val callCommandHandler: CallCommandHandler
) {

    private var pendingContacts: List<Contact>? = null

    fun route(text: String): CommandRouteResult {
        val pending = pendingContacts

        if (pending != null) {
            val selection = text.trim().lowercase()

            if (
                selection == "cancel" ||
                selection == "cancel call" ||
                selection == "never mind" ||
                selection == "nevermind"
            ) {
                pendingContacts = null
                return CommandRouteResult.SelectionCancelled
            }

            val number = parseSelection(selection)

            if (number == null || number !in 1..pending.size) {
                return CommandRouteResult.SelectionInvalid(
                    contactNames = pending.map { it.displayName }
                )
            }

            val selectedContact = pending[number - 1]
            pendingContacts = null

            return mapResult(
                callCommandHandler.callContact(selectedContact)
            )
        }

        return mapResult(callCommandHandler.handle(text))
    }

    private fun mapResult(
        result: CallCommandHandlerResult
    ): CommandRouteResult {
        return when (result) {
            CallCommandHandlerResult.NotACommand ->
                CommandRouteResult.NotHandled

            is CallCommandHandlerResult.Called ->
                CommandRouteResult.Called(
                    contactName = result.contact.displayName,
                    phoneNumber = result.phoneNumber.number
                )

            is CallCommandHandlerResult.Ambiguous -> {
                pendingContacts = result.contacts

                CommandRouteResult.Ambiguous(
                    contactNames = result.contacts.map {
                        it.displayName
                    }
                )
            }

            CallCommandHandlerResult.NotFound ->
                CommandRouteResult.NotFound

            CallCommandHandlerResult.NoPhoneNumber ->
                CommandRouteResult.NoPhoneNumber

            CallCommandHandlerResult.CallFailed ->
                CommandRouteResult.CallFailed
        }
    }

    private fun parseSelection(text: String): Int? {
        text.toIntOrNull()?.let { return it }

        val normalized = text
            .replace(Regex("[^a-z0-9 ]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val words = normalized.split(" ")

        val numberWords = mapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6,
            "seven" to 7,
            "eight" to 8,
            "nine" to 9,
            "ten" to 10,
            "eleven" to 11,
            "twelve" to 12,
            "thirteen" to 13,
            "fourteen" to 14,
            "fifteen" to 15,
            "sixteen" to 16,
            "seventeen" to 17,
            "eighteen" to 18,
            "nineteen" to 19,
            "twenty" to 20
        )

        if (words.size == 1) {
            return numberWords[words[0]]
        }

        if (words.size == 2 && words[0] == "number") {
            return numberWords[words[1]]
        }

        return null
    }
}

sealed interface CommandRouteResult {

    data class Called(
        val contactName: String,
        val phoneNumber: String
    ) : CommandRouteResult

    data class Ambiguous(
        val contactNames: List<String>
    ) : CommandRouteResult

    data class SelectionInvalid(
        val contactNames: List<String>
    ) : CommandRouteResult

    data object SelectionCancelled : CommandRouteResult

    data object NotFound : CommandRouteResult

    data object NoPhoneNumber : CommandRouteResult

    data object CallFailed : CommandRouteResult

    data object NotHandled : CommandRouteResult
}
