package com.aalavandaanus.kabaliassistant.features.calling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallCommandParserTest {


    private val parser = CallCommandParser()

    @Test
    fun `parse recognizes call command`() {
        val result = parser.parse("Call Muthu")

        assertTrue(result is CallCommandParseResult.Call)

        result as CallCommandParseResult.Call

        assertEquals("Muthu", result.spokenName)
    }

    @Test
    fun `parse recognizes prefixed contact name`() {
        val result = parser.parse("Call ACC Muthu")

        assertTrue(result is CallCommandParseResult.Call)

        result as CallCommandParseResult.Call

        assertEquals("ACC Muthu", result.spokenName)
    }

    @Test
    fun `parse recognizes lowercase call command`() {
        val result = parser.parse("call REL Muthu")

        assertTrue(result is CallCommandParseResult.Call)

        result as CallCommandParseResult.Call

        assertEquals("REL Muthu", result.spokenName)
    }

    @Test
    fun `parse ignores extra spaces`() {
        val result = parser.parse("  Call    ACC Muthu  ")

        assertTrue(result is CallCommandParseResult.Call)

        result as CallCommandParseResult.Call

        assertEquals("ACC Muthu", result.spokenName)
    }

    @Test
    fun `parse returns not a command for unrelated text`() {
        val result = parser.parse("What is the weather")

        assertTrue(
            result is CallCommandParseResult.NotACommand
        )
    }

    @Test
    fun `parse returns not a command for call without name`() {
        val result = parser.parse("Call")

        assertTrue(
            result is CallCommandParseResult.NotACommand
        )
    }


}
