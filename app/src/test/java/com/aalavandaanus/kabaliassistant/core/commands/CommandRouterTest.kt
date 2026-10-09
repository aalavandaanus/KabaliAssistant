package com.aalavandaanus.kabaliassistant.core.commands

import com.aalavandaanus.kabaliassistant.features.calling.CallCommandHandler
import com.aalavandaanus.kabaliassistant.features.calling.CallCommandParser
import com.aalavandaanus.kabaliassistant.features.calling.CallContactCommand
import com.aalavandaanus.kabaliassistant.features.calling.Contact
import com.aalavandaanus.kabaliassistant.features.calling.ContactRepository
import com.aalavandaanus.kabaliassistant.features.calling.DefaultContactResolver
import com.aalavandaanus.kabaliassistant.features.calling.PhoneNumber
import com.aalavandaanus.kabaliassistant.features.calling.PhoneNumberType
import com.aalavandaanus.kabaliassistant.features.calling.CallService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandRouterTest {


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
    fun `route handles call command`() {

        val callService = FakeCallService(true)

        val handler = createHandler(
            contacts = listOf(accMuthu),
            callService = callService
        )

        val router = CommandRouter(handler)

        val result = router.route("Call ACC Muthu")

        assertTrue(
            result is CommandRouteResult.Called
        )

        result as CommandRouteResult.Called

        assertEquals(
            "ACC Muthu",
            result.contactName
        )

        assertEquals(
            "1111111111",
            result.phoneNumber
        )

        assertEquals(
            "1111111111",
            callService.lastNumber
        )
    }

    @Test
    fun `route returns ambiguous for multiple matches`() {

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

        val handler = createHandler(
            contacts = listOf(
                accMuthu,
                relMuthu
            ),
            callService = FakeCallService(true)
        )

        val router = CommandRouter(handler)

        val result = router.route("Call Muthu")

        assertTrue(
            result is CommandRouteResult.Ambiguous
        )

        result as CommandRouteResult.Ambiguous

        assertEquals(
            listOf("ACC Muthu", "REL Muthu"),
            result.contactNames
        )
    }

    @Test
    fun `route ignores unrelated text`() {

        val handler = createHandler(
            contacts = listOf(accMuthu),
            callService = FakeCallService(true)
        )

        val router = CommandRouter(handler)

        val result =
            router.route("What is the weather")

        assertTrue(
            result is CommandRouteResult.NotHandled
        )
    }

    private fun createHandler(
        contacts: List<Contact>,
        callService: CallService
    ): CallCommandHandler {

        val command = CallContactCommand(
            contactRepository =
                FakeContactRepository(contacts),
            contactResolver =
                DefaultContactResolver(),
            callService = callService
        )

        return CallCommandHandler(
            parser = CallCommandParser(),
            callContactCommand = command
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
