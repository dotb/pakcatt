package pakcatt.application.mailbox

import org.springframework.stereotype.Component
import pakcatt.util.StringUtils

@Component
class MailboxStore {

    private var stringUtils = StringUtils()
    private var messages = ArrayList<MailMessage>()
    private var messageCounter = 1

    fun messageListForCallsign(userCallsign: String): List<MailMessage> {
        return filteredMessages(userCallsign)
    }

    fun storeMessage(message: MailMessage) {
        message.messageNumber = messageCounter
        messageCounter++
        messages.add(message)
    }

    fun getMessage(userCallsign: String, messageNumber: Int): MailMessage? {
        val message = messageNumbered(userCallsign, messageNumber)
        return message
    }

    fun deleteMessage(userCallsign: String, messageNumber: Int): MailMessage? {
        val message = messageNumbered(userCallsign, messageNumber)
        if (null != message) {
            messages.remove(message)
            return message
        }
        return null
    }

    private fun messageNumbered(userCallsign: String, messageNumber: Int): MailMessage? {
        val usersMessages = filteredMessages(userCallsign)
        for (message in usersMessages) {
            if (message.messageNumber == messageNumber) {
                return message
            }
        }
        return null
    }

    private fun filteredMessages(userCallsign: String): List<MailMessage> {
        val formattedUserCallsign = stringUtils.formatCallsignRemoveSSID(userCallsign)
        var filteredMessages = ArrayList<MailMessage>()
        for (message in messages) {
            if (message.toCallsign.compareTo(formattedUserCallsign, true) == 0 || message.fromCallsign.compareTo(formattedUserCallsign, true) == 0) {
                filteredMessages.add(message)
            }
        }
        return filteredMessages
    }

}