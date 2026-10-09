package com.aalavandaanus.kabaliassistant.features.calling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactResolverTest {

    private val resolver = DefaultContactResolver()

    private val contacts = listOf(

        Contact(
            id = 1L,
            displayName = "ACC Muthu",
            phoneNumbers = listOf(
                PhoneNumber(
                    number = "1111111111",
                    type = PhoneNumberType.MOBILE
                ),
                PhoneNumber(
                    number = "2222222222",
                    type = PhoneNumberType.WORK
                )
            )
        ),

        Contact(
            id = 2L,
            displayName = "REL Santhosh",
            phoneNumbers = listOf(
                PhoneNumber(
                    number = "3333333333",
                    type = PhoneNumberType.MOBILE
                )
            )
        ),

        Contact(
            id = 3L,
            displayName = "FRD Kumar",
            phoneNumbers = listOf(
                PhoneNumber(
                    number = "4444444444",
                    type = PhoneNumberType.MOBILE
                )
            )
        )
    )

    @Test
    fun `resolves exact full contact name`() {

        val result = resolver.resolve(
            spokenName = "ACC Muthu",
            contacts = contacts
        )

        assertTrue(
            result is ContactResolutionResult.Found
        )

        val found =
            result as ContactResolutionResult.Found

        assertEquals(
            "ACC Muthu",
            found.contact.displayName
        )

        assertEquals(
            2,
            found.contact.phoneNumbers.size
        )
    }

    @Test
    fun `resolves person name after context prefix`() {

        val result = resolver.resolve(
            spokenName = "Muthu",
            contacts = contacts
        )

        assertTrue(
            result is ContactResolutionResult.Found
        )

        val found =
            result as ContactResolutionResult.Found

        assertEquals(
            "ACC Muthu",
            found.contact.displayName
        )
    }

    @Test
    fun `resolves relative by person name`() {

        val result = resolver.resolve(
            spokenName = "Santhosh",
            contacts = contacts
        )

        assertTrue(
            result is ContactResolutionResult.Found
        )

        val found =
            result as ContactResolutionResult.Found

        assertEquals(
            "REL Santhosh",
            found.contact.displayName
        )
    }

    @Test
    fun `returns not found when contact does not exist`() {

        val result = resolver.resolve(
            spokenName = "Ravi",
            contacts = contacts
        )

        assertTrue(
            result is ContactResolutionResult.NotFound
        )
    }

    @Test
    fun `returns ambiguous when multiple contacts have same person name`() {

        val ambiguousContacts =
            contacts + Contact(
                id = 4L,
                displayName = "REL Muthu",
                phoneNumbers = listOf(
                    PhoneNumber(
                        number = "5555555555",
                        type = PhoneNumberType.MOBILE
                    )
                )
            )

        val result = resolver.resolve(
            spokenName = "Muthu",
            contacts = ambiguousContacts
        )

        assertTrue(
            result is ContactResolutionResult.Ambiguous
        )

        val ambiguous =
            result as ContactResolutionResult.Ambiguous

        assertEquals(
            2,
            ambiguous.contacts.size
        )
    }

    @Test
    fun `matching ignores case`() {

        val result = resolver.resolve(
            spokenName = "mUtHu",
            contacts = contacts
        )

        assertTrue(
            result is ContactResolutionResult.Found
        )

        val found =
            result as ContactResolutionResult.Found

        assertEquals(
            "ACC Muthu",
            found.contact.displayName
        )
    }

    @Test
    fun `matching ignores extra spaces`() {

        val result = resolver.resolve(
            spokenName = "  Muthu  ",
            contacts = contacts
        )

        assertTrue(
            result is ContactResolutionResult.Found
        )

        val found =
            result as ContactResolutionResult.Found

        assertEquals(
            "ACC Muthu",
            found.contact.displayName
        )
    }

    @Test
    fun `preserves multiple phone numbers for a contact`() {

        val result = resolver.resolve(
            spokenName = "Muthu",
            contacts = contacts
        )

        assertTrue(
            result is ContactResolutionResult.Found
        )

        val found =
            result as ContactResolutionResult.Found

        assertEquals(
            2,
            found.contact.phoneNumbers.size
        )

        assertEquals(
            PhoneNumberType.MOBILE,
            found.contact.phoneNumbers[0].type
        )

        assertEquals(
            PhoneNumberType.WORK,
            found.contact.phoneNumbers[1].type
        )
    }
}