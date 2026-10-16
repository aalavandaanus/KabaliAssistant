package com.aalavandaanus.kabaliassistant.core.commands

/**
 * Recognizes a small set of explicitly supported local command phrases.
 *
 * Interpretation only: this class never reads data or executes an action.
 */
class LocalCommandInterpreter {

    fun interpret(transcript: String): CommandInterpretation {
        val normalized = normalize(transcript)

        val intent = when (normalized) {
            "show my alarms",
            "list my alarms",
            "what alarms do i have" -> CommandIntent.LIST_ALARMS
            else -> CommandIntent.UNKNOWN
        }

        return CommandInterpretation(
            originalTranscript = transcript,
            normalizedText = normalized,
            intent = intent,
            confidence = if (intent == CommandIntent.UNKNOWN) {
                CommandConfidence.LOW
            } else {
                CommandConfidence.HIGH
            },
            language = if (intent == CommandIntent.UNKNOWN) {
                CommandLanguage.UNKNOWN
            } else {
                CommandLanguage.ENGLISH
            }
        )
    }

    private fun normalize(transcript: String): String =
        transcript
            .lowercase()
            .replace(Regex("[^\\p{L}\\p{N}\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
}
