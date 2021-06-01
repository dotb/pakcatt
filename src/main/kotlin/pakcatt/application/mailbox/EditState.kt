package pakcatt.application.mailbox

class EditState(val userCallsign: String,
                var state: MessageEditState = MessageEditState.EDITING_SUBJECT,
                var message: MailMessage = MailMessage(userCallsign)) {

    enum class MessageEditState {
        EDITING_SUBJECT, EDITING_MESSAGE
    }
}