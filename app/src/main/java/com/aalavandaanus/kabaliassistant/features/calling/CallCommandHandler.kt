package com.aalavandaanus.kabaliassistant.features.calling

sealed interface CallCommandHandlerResult {

    data class Called(
        val contact: Contact,
        val phoneNumber: PhoneNumber
    ) : CallCommandHandlerResult

    data class Ambiguous(
        val contacts: List<Contact>
    ) : CallCommandHandlerResult

    data object NotFound : CallCommandHandlerResult

    data object NoPhoneNumber : CallCommandHandlerResult

    data object CallFailed : CallCommandHandlerResult

    data object NotACommand : CallCommandHandlerResult
}

class CallCommandHandler(
    private val parser: CallCommandParser,
    private val callContactCommand: CallContactCommand
) {

    fun handle(text: String): CallCommandHandlerResult {
        return when (val parsed = parser.parse(text)) {
            CallCommandParseResult.NotACommand ->
                CallCommandHandlerResult.NotACommand

            is CallCommandParseResult.Call ->
                mapResult(
                    callContactCommand.execute(parsed.spokenName)
                )
        }
    }

    fun callContact(contact: Contact): CallCommandHandlerResult {
        return mapResult(callContactCommand.executeContact(contact))
    }

    private fun mapResult(
        result: CallContactResult
    ): CallCommandHandlerResult {
        return when (result) {
            is CallContactResult.Called ->
                CallCommandHandlerResult.Called(
                    contact = result.contact,
                    phoneNumber = result.phoneNumber
                )

            is CallContactResult.Ambiguous ->
                CallCommandHandlerResult.Ambiguous(result.contacts)

            CallContactResult.NotFound ->
                CallCommandHandlerResult.NotFound

            CallContactResult.NoPhoneNumber ->
                CallCommandHandlerResult.NoPhoneNumber

            CallContactResult.CallFailed ->
                CallCommandHandlerResult.CallFailed
        }
    }
}
