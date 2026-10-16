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
}
