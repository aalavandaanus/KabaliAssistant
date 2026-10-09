package com.aalavandaanus.kabaliassistant.features.calling

sealed interface CallCommandParseResult {

    data class Call(
        val spokenName: String
    ) : CallCommandParseResult

    data object NotACommand : CallCommandParseResult
}

class CallCommandParser {

    fun parse(
        text: String
    ): CallCommandParseResult {

        val normalized = text
            .trim()
            .replace(Regex("\\s+"), " ")

        if (normalized.isBlank()) {
            return CallCommandParseResult.NotACommand
        }

        val match = Regex(
            pattern = "^call\\s+(.+)$",
            option = RegexOption.IGNORE_CASE
        ).find(normalized)
            ?: return CallCommandParseResult.NotACommand

        val spokenName = match
            .groupValues[1]
            .trim()

        if (spokenName.isBlank()) {
            return CallCommandParseResult.NotACommand
        }

        return CallCommandParseResult.Call(
            spokenName = spokenName
        )
    }
}