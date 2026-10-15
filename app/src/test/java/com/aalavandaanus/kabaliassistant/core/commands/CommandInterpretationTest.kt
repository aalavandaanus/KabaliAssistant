package com.aalavandaanus.kabaliassistant.core.commands

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandInterpretationTest {

    @Test
    fun `keeps original transcript separate from normalized text`() {
        val command = CommandInterpretation(
            originalTranscript = "Naalaikku seven-ku alarm podu",
            normalizedText = "set alarm tomorrow at seven",
            intent = CommandIntent.CREATE_ALARM,
            confidence = CommandConfidence.HIGH,
            language = CommandLanguage.TANGLISH
        )

        assertEquals("Naalaikku seven-ku alarm podu", command.originalTranscript)
        assertEquals("set alarm tomorrow at seven", command.normalizedText)
        assertNotEquals(command.originalTranscript, command.normalizedText)
    }

    @Test
    fun `represents missing details without inventing parameter values`() {
        val command = CommandInterpretation(
            originalTranscript = "Remind me to call Amma tomorrow",
            normalizedText = "remind me to call amma tomorrow",
            intent = CommandIntent.CREATE_REMINDER,
            parameters = mapOf(
                "task" to "Call Amma",
                "date" to "tomorrow"
            ),
            confidence = CommandConfidence.MEDIUM,
            language = CommandLanguage.ENGLISH,
            missingParameters = setOf("time")
        )

        assertEquals("Call Amma", command.parameters["task"])
        assertEquals("tomorrow", command.parameters["date"])
        assertTrue(command.missingParameters.contains("time"))
        assertEquals(null, command.parameters["time"])
    }

    @Test
    fun `defaults language and conversation relation safely`() {
        val command = CommandInterpretation(
            originalTranscript = "unknown phrase",
            normalizedText = "unknown phrase",
            intent = CommandIntent.UNKNOWN,
            confidence = CommandConfidence.LOW
        )

        assertEquals(CommandLanguage.UNKNOWN, command.language)
        assertEquals(ConversationRelation.NEW_REQUEST, command.conversationRelation)
    }

    @Test
    fun `records a follow up as related to the active interaction`() {
        val command = CommandInterpretation(
            originalTranscript = "morning ten-ku",
            normalizedText = "10 AM",
            intent = CommandIntent.CREATE_REMINDER,
            parameters = mapOf("time" to "10:00 AM"),
            confidence = CommandConfidence.HIGH,
            language = CommandLanguage.TANGLISH,
            conversationRelation = ConversationRelation.FOLLOW_UP
        )

        assertEquals(ConversationRelation.FOLLOW_UP, command.conversationRelation)
        assertEquals("10:00 AM", command.parameters["time"])
    }
}
