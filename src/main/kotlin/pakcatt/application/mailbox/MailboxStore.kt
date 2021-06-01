package pakcatt.application.mailbox

import org.springframework.stereotype.Component

@Component
class MailboxStore {

    private var messages = ArrayList<MailMessage>()

    fun messagesForCallsign(userCallsign: String): List<MailMessage> {
        var filteredMessages = ArrayList<MailMessage>()
        for (message in messages) {
            if (message.toCallsign.compareTo(userCallsign, true) ==0 || message.fromCallsign.compareTo(userCallsign, true) == 0) {
                filteredMessages.add(message)
            }
        }
        return filteredMessages
    }

    fun storeMessage(message: MailMessage) {
        messages.add(message)
    }

}