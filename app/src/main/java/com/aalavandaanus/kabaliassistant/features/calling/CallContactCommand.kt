package com.aalavandaanus.kabaliassistant.features.calling

sealed interface CallContactResult {

    data class Called(
        val contact: Contact,
        val phoneNumber: PhoneNumber
    ) : CallContactResult

    data class Ambiguous(
        val contacts: List<Contact>
    ) : CallContactResult

    data object NotFound : CallContactResult

    data object NoPhoneNumber : CallContactResult

    data object CallFailed : CallContactResult
}

class CallContactCommand(
    private val contactRepository: ContactRepository,
    private val contactResolver: ContactResolver,
    private val callService: CallService
) {

    fun execute(spokenName: String): CallContactResult {
        val contacts = contactRepository.getContacts()

        return when (
            val resolution = contactResolver.resolve(
                spokenName = spokenName,
                contacts = contacts
            )
        ) {
            is ContactResolutionResult.Found ->
                executeContact(resolution.contact)

            is ContactResolutionResult.Ambiguous ->
                CallContactResult.Ambiguous(resolution.contacts)

            ContactResolutionResult.NotFound ->
                CallContactResult.NotFound
        }
    }

    fun executeContact(contact: Contact): CallContactResult {
        val phoneNumber = contact.phoneNumbers.firstOrNull()
            ?: return CallContactResult.NoPhoneNumber

        return if (callService.call(phoneNumber.number)) {
            CallContactResult.Called(
                contact = contact,
                phoneNumber = phoneNumber
            )
        } else {
            CallContactResult.CallFailed
        }
    }
}
