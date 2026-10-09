package com.aalavandaanus.kabaliassistant.features.calling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallContactCommandTest {

    private val accMuthu = Contact(
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
    )

    @Test
    fun `execute returns Called when contact is found and call succeeds`() {

        val repository = FakeContactRepository(
            contacts = listOf(accMuthu)
        )

        val resolver = FakeContactResolver(
            result = ContactResolutionResult.Found(accMuthu)
        )

        val callService = FakeCallService(
            result = true
        )

        val command = CallContactCommand(
            contactRepository = repository,
            contactResolver = resolver,
            callService = callService
        )

        val result = command.execute("ACC Muthu")

        assertTrue(
            result is CallContactResult.Called
        )

        result as CallContactResult.Called

        assertEquals(
            "ACC Muthu",
            result.contact.displayName
        )

        assertEquals(
            "1111111111",
            result.phoneNumber.number
        )

        assertEquals(
            "1111111111",
            callService.lastNumber
        )
    }

    @Test
    fun `execute returns Ambiguous when multiple contacts match`() {

        val relMuthu = Contact(
            id = 2L,
            displayName = "REL Muthu",
            phoneNumbers = listOf(
                PhoneNumber(
                    number = "3333333333",
                    type = PhoneNumberType.MOBILE
                )
            )
        )

        val repository = FakeContactRepository(
            contacts = listOf(
                accMuthu,
                relMuthu
            )
        )

        val resolver = FakeContactResolver(
            result = ContactResolutionResult.Ambiguous(
                contacts = listOf(
                    accMuthu,
                    relMuthu
                )
            )
        )

        val callService = FakeCallService(
            result = true
        )

        val command = CallContactCommand(
            contactRepository = repository,
            contactResolver = resolver,
            callService = callService
        )

        val result = command.execute("Muthu")

        assertTrue(
            result is CallContactResult.Ambiguous
        )

        result as CallContactResult.Ambiguous

        assertEquals(
            2,
            result.contacts.size
        )

        assertEquals(
            "ACC Muthu",
            result.contacts[0].displayName
        )

        assertEquals(
            "REL Muthu",
            result.contacts[1].displayName
        )

        assertEquals(
            null,
            callService.lastNumber
        )
    }

    @Test
    fun `execute returns NotFound when contact cannot be resolved`() {

        val repository = FakeContactRepository(
            contacts = listOf(accMuthu)
        )

        val resolver = FakeContactResolver(
            result = ContactResolutionResult.NotFound
        )

        val callService = FakeCallService(
            result = true
        )

        val command = CallContactCommand(
            contactRepository = repository,
            contactResolver = resolver,
            callService = callService
        )

        val result = command.execute("Unknown")

        assertTrue(
            result is CallContactResult.NotFound
        )

        assertEquals(
            null,
            callService.lastNumber
        )
    }

    @Test
    fun `execute returns NoPhoneNumber when contact has no phone numbers`() {

        val contact = Contact(
            id = 3L,
            displayName = "No Phone",
            phoneNumbers = emptyList()
        )

        val repository = FakeContactRepository(
            contacts = listOf(contact)
        )

        val resolver = FakeContactResolver(
            result = ContactResolutionResult.Found(contact)
        )

        val callService = FakeCallService(
            result = true
        )

        val command = CallContactCommand(
            contactRepository = repository,
            contactResolver = resolver,
            callService = callService
        )

        val result = command.execute("No Phone")

        assertTrue(
            result is CallContactResult.NoPhoneNumber
        )

        assertEquals(
            null,
            callService.lastNumber
        )
    }

    @Test
    fun `execute returns CallFailed when call service fails`() {

        val repository = FakeContactRepository(
            contacts = listOf(accMuthu)
        )

        val resolver = FakeContactResolver(
            result = ContactResolutionResult.Found(accMuthu)
        )

        val callService = FakeCallService(
            result = false
        )

        val command = CallContactCommand(
            contactRepository = repository,
            contactResolver = resolver,
            callService = callService
        )

        val result = command.execute("ACC Muthu")

        assertTrue(
            result is CallContactResult.CallFailed
        )

        assertEquals(
            "1111111111",
            callService.lastNumber
        )
    }

    @Test
    fun `execute uses first phone number when contact has multiple numbers`() {

        val repository = FakeContactRepository(
            contacts = listOf(accMuthu)
        )

        val resolver = FakeContactResolver(
            result = ContactResolutionResult.Found(accMuthu)
        )

        val callService = FakeCallService(
            result = true
        )

        val command = CallContactCommand(
            contactRepository = repository,
            contactResolver = resolver,
            callService = callService
        )

        command.execute("ACC Muthu")

        assertEquals(
            "1111111111",
            callService.lastNumber
        )
    }

    private class FakeContactRepository(
        private val contacts: List<Contact>
    ) : ContactRepository {

        override fun getContacts(): List<Contact> {
            return contacts
        }
    }

    private class FakeContactResolver(
        private val result: ContactResolutionResult
    ) : ContactResolver {

        override fun resolve(
            spokenName: String,
            contacts: List<Contact>
        ): ContactResolutionResult {
            return result
        }
    }

    private class FakeCallService(
        private val result: Boolean
    ) : CallService {

        var lastNumber: String? = null
            private set

        override fun call(
            phoneNumber: String
        ): Boolean {

            lastNumber = phoneNumber

            return result
        }
    }
}
