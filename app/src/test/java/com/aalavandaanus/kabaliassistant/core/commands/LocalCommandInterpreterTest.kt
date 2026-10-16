package com.aalavandaanus.kabaliassistant.core.commands

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalCommandInterpreterTest {

    @Test
    fun `recognizes show my alarms as a list alarms command`() {
        val result = LocalCommandInterpreter().interpret("Show my alarms")

        assertEquals(CommandIntent.LIST_ALARMS, result.intent)
        assertEquals("Show my alarms", result.originalTranscript)
    }

    @Test
    fun `recognizes list my alarms as a list alarms command`() {
        val result = LocalCommandInterpreter().interpret("List my alarms")

        assertEquals(CommandIntent.LIST_ALARMS, result.intent)
        assertEquals("List my alarms", result.originalTranscript)
    }

    @Test
    fun `recognizes what alarms do I have as a list alarms command`() {
        val result = LocalCommandInterpreter().interpret("What alarms do I have?")

        assertEquals(CommandIntent.LIST_ALARMS, result.intent)
        assertEquals("What alarms do I have?", result.originalTranscript)
    }

    @Test
    fun `does not treat a negated alarm statement as a list alarms command`() {
        val result = LocalCommandInterpreter().interpret("I don't want to see my alarms")

        assertEquals(CommandIntent.UNKNOWN, result.intent)
    }
}
