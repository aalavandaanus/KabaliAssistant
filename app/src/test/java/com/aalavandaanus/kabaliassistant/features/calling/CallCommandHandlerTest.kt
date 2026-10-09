package com.aalavandaanus.kabaliassistant.features.calling

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallCommandHandlerTest {


    private val accMuthu = Contact(
        id = 1L,
        displayName = "ACC Muthu",
        phoneNumbers = listOf(
            PhoneNumber(
                number = "1111111111",
                type = PhoneNumberType.MOBILE
            )
        )
    )

    @Test
    fun `handle calls contact for valid call command`() {

        val callService = FakeCallService(true)

        val command = CallContactCommand(
            contactRepository = FakeContactRepository(
                listOf(accMuthu)
            ),
            contactResolver = DefaultContactResolver(),
            callService = callService
        )

        val handler = CallCommandHandler(
            parser = CallCommandParser(),
            callContactCommand = command
        )

        val result = handler.handle("Call ACC Muthu")

        assertTrue(
            result is CallCommandHandlerResult.Called
        )

        result as CallCommandHandlerResult.Called

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
    fun `handle returns ambiguous when name matches multiple contacts`() {

        val relMuthu = Contact(
            id = 2L,
            displayName = "REL Muthu",
            phoneNumbers = listOf(
                PhoneNumber(
                    number = "2222222222",
                    type = PhoneNumberType.MOBILE
                )
            )
        )

        val command = CallContactCommand(
            contactRepository = FakeContactRepository(
                listOf(accMuthu, relMuthu)
            ),
            contactResolver = DefaultContactResolver(),
            callService = FakeCallService(true)
        )

        val handler = CallCommandHandler(
            parser = CallCommandParser(),
            callContactCommand = command
        )

        val result = handler.handle("Call Muthu")

        assertTrue(
            result is CallCommandHandlerResult.Ambiguous
        )

        result as CallCommandHandlerResult.Ambiguous

        assertEquals(2, result.contacts.size)
    }

    @Test
    fun `handle ignores non call command`() {

        val command = CallContactCommand(
            contactRepository = FakeContactRepository(
                listOf(accMuthu)
            ),
            contactResolver = DefaultContactResolver(),
            callService = FakeCallService(true)
        )

        val handler = CallCommandHandler(
            parser = CallCommandParser(),
            callContactCommand = command
        )

        val result =
            handler.handle("What is the weather")

        assertTrue(
            result is CallCommandHandlerResult.NotACommand
        )
    }

    private class FakeContactRepository(
        private val contacts: List<Contact>
    ) : ContactRepository {

        override fun getContacts(): List<Contact> =
            contacts
    }

    private class FakeCallService(
        private val result: Boolean
    ) : CallService {

        var lastNumber: String? = null

        override fun call(
            phoneNumber: String
        ): Boolean {

            lastNumber = phoneNumber

            return result
        }
    }


}
