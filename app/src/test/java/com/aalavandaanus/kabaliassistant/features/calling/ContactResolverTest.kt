package com.aalavandaanus.kabaliassistant.features.calling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactResolverTest {

    private val resolver = ContactResolver()

    private val contacts = listOf(
        Contact(
            id = 1L,
            displayName = "ACC Muthu",
            phoneNumber = "1111111111"
        ),
        Contact(
            id = 2L,
            displayName = "REL Santhosh",
            phoneNumber = "2222222222"
        ),
        Contact(
            id = 3L,
            displayName = "FRD Kumar",
            phoneNumber = "3333333333"
        )
    )

    @Test
    fun `resolves exact full contact name`() {
        val result = resolver.resolve(
            spokenName = "ACC Muthu",
            contacts = contacts
        )

        assertTrue(result is ContactResolutionResult.Found)

        val found = result as ContactResolutionResult.Found

        assertEquals("ACC Muthu", found.contact.displayName)
    }

    @Test
    fun `resolves person name after context prefix`() {
        val result = resolver.resolve(
            spokenName = "Muthu",
            contacts = contacts
        )

        assertTrue(result is ContactResolutionResult.Found)

        val found = result as ContactResolutionResult.Found

        assertEquals("ACC Muthu", found.contact.displayName)
    }

    @Test
    fun `resolves relative by person name`() {
        val result = resolver.resolve(
            spokenName = "Santhosh",
            contacts = contacts
        )

        assertTrue(result is ContactResolutionResult.Found)

        val found = result as ContactResolutionResult.Found

        assertEquals("REL Santhosh", found.contact.displayName)
    }

    @Test
    fun `returns not found when contact does not exist`() {
        val result = resolver.resolve(
            spokenName = "Ravi",
            contacts = contacts
        )

        assertTrue(result is ContactResolutionResult.NotFound)
    }

    @Test
    fun `returns ambiguous when multiple contacts have same person name`() {
        val ambiguousContacts = contacts + Contact(
            id = 4L,
            displayName = "REL Muthu",
            phoneNumber = "4444444444"
        )

        val result = resolver.resolve(
            spokenName = "Muthu",
            contacts = ambiguousContacts
        )

        assertTrue(result is ContactResolutionResult.Ambiguous)

        val ambiguous = result as ContactResolutionResult.Ambiguous

        assertEquals(2, ambiguous.contacts.size)
    }

    @Test
    fun `matching ignores case`() {
        val result = resolver.resolve(
            spokenName = "mUtHu",
            contacts = contacts
        )

        assertTrue(result is ContactResolutionResult.Found)

        val found = result as ContactResolutionResult.Found

        assertEquals("ACC Muthu", found.contact.displayName)
    }

    @Test
    fun `matching ignores extra spaces`() {
        val result = resolver.resolve(
            spokenName = "  Muthu  ",
            contacts = contacts
        )

        assertTrue(result is ContactResolutionResult.Found)

        val found = result as ContactResolutionResult.Found

        assertEquals("ACC Muthu", found.contact.displayName)
    }
}