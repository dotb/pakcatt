package pakcatt.application.mailbox.persistence

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import pakcatt.util.StringUtils

@Component
class MailboxStore(val mailMessageRepository: MailMessageRepository) {

    private var stringUtils = StringUtils()

    fun messageListForCallsign(userCallsign: String): List<MailMessage> {
        return filteredMessages(userCallsign)
    }

    fun storeMessage(message: MailMessage) {
        var nextMessageNumber = 1
        val allMessages = mailMessageRepository.findAll(Sort.by(Sort.Direction.DESC, "messageNumber"))
        if (allMessages.size > 0) {
            val lastMessage = allMessages.first()
            nextMessageNumber = lastMessage.messageNumber + 1
        }
        message.messageNumber = nextMessageNumber
        mailMessageRepository.insert(message)
    }

    fun getMessage(userCallsign: String, messageNumber: Int): MailMessage? {
        return messageNumbered(userCallsign, messageNumber)
    }

    fun deleteMessage(userCallsign: String, messageNumber: Int): MailMessage? {
        val message = messageNumbered(userCallsign, messageNumber)
        if (null != message) {
            mailMessageRepository.delete(message)
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

    private fun filteredMessages(forCallsign: String): List<MailMessage> {
        val formattedUserCallsign = stringUtils.formatCallsignRemoveSSID(forCallsign)
        return mailMessageRepository.findByFromCallsignOrToCallsign(formattedUserCallsign, formattedUserCallsign)
    }

}