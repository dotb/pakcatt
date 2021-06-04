package pakcatt.application.mailbox

import org.springframework.stereotype.Component

@Component
class MailboxStore {

    private var messages = ArrayList<MailMessage>()
    private var messageCounter = 1

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
        message.messageNumber = messageCounter
        messageCounter++
        messages.add(message)
    }

    fun getMessage(messageNumber: Int): MailMessage? {
        val message = messageNumbered(messageNumber)
        return message
    }

    fun deleteMessage(messageNumber: Int): MailMessage? {
        val message = messageNumbered(messageNumber)
        if (null != message) {
            messages.remove(message)
            return message
        }
        return null
    }

    private fun messageNumbered(messageNumber: Int): MailMessage? {
        for (message in messages) {
            if (message.messageNumber == messageNumber) {
                return message
            }
        }
        return null
    }

}