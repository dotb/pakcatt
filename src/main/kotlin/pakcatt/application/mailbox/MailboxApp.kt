package pakcatt.application.mailbox

import org.slf4j.LoggerFactory
import pakcatt.application.mailbox.edit.EditSubjectApp
import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.*
import pakcatt.application.shared.command.Command
import pakcatt.network.packet.protocol.no_layer_three.model.LinkRequest
import pakcatt.network.packet.protocol.no_layer_three.model.LinkResponse
import pakcatt.util.StringUtils
import java.lang.StringBuilder
import java.text.SimpleDateFormat

class MailboxApp(private val mailboxStore: MailboxStore): SubApp() {

    private val tabSpace = "\t"

    init {
        registerCommand(Command("list") .function { listMessages(it) }  .description("List the messages available to you"))
        registerCommand(Command("send") .function { sendMessage(it) }   .description("Send a message, passing the destination callsign as an argument"))
        registerCommand(Command("read") .function { readMessage(it) }   .description("Read a single message, passing the message number as an argument"))
        registerCommand(Command("del")  .function { deleteMessage(it) } .description("Delete a message, passing the message number as an argument"))
        registerCommand(Command("quit") .reply("Bye").openApp(NavigateBack(1)).description("Leave the mail app and return to the main menu"))
    }

    override fun returnCommandPrompt(): String {
        return "mail>"
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        return handleRequestWithRegisteredCommand(request)
    }

    fun unreadMessageCount(request: LinkRequest): Int {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        return mailboxStore.getUnreadMessagesTo(formattedCallsign).size
    }

    private fun listMessages(request: LinkRequest): LinkResponse {
        val userMessages = mailboxStore.messageListForCallsign(request.remoteCallsign)
        val listResponse = StringBuilder()
        val messageCount = userMessages.size
        val dateFormatter = SimpleDateFormat("dd MMM HH:mm")

        if (messageCount > 0) {
            listResponse.append(StringUtils.EOL)
            listResponse.append("  No${tabSpace}Date          From${tabSpace}To${tabSpace}Subject${StringUtils.EOL}")
            for (message in userMessages) {
                listResponse.append(when (message.isRead) {
                    true -> "  "
                    false -> "* "
                })
                listResponse.append(message.messageNumber)
                listResponse.append(tabSpace)
                listResponse.append(dateFormatter.format(message.dateTime.time))
                listResponse.append("  ")
                listResponse.append(message.fromCallsign)
                listResponse.append(tabSpace)
                listResponse.append(message.toCallsign)
                listResponse.append(tabSpace)
                listResponse.append(message.subject)
                listResponse.append(StringUtils.EOL)
            }
        }
        listResponse.append(messageCount)
        listResponse.append(" messages")
        listResponse.append(StringUtils.EOL)
        return LinkResponse.sendText(listResponse.toString())
    }

    private fun readMessage(request: LinkRequest): LinkResponse {
        val userCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        var message: MailMessage? = null
        val messageNumber = parseIntArgument(request.message)
        if (null != messageNumber) {
            message = mailboxStore.getMessage(userCallsign, messageNumber)
        }
        return if (null != message) {
            // Mark this message as read if it's being accessed by the recipient
            if (userCallsign == message.toCallsign) {
                message.isRead = true
                mailboxStore.updateMessage(message)
            }
            LinkResponse.sendText("${StringUtils.EOL}Subject: ${message.subject}${StringUtils.EOL}${message.body.toString()}")
        } else {
            LinkResponse.sendText("No message found")
        }
    }

    private fun sendMessage(request: LinkRequest): LinkResponse {
        val arg = parseStringArgument(request.message, "")
        val fromCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val toCallsign = stringUtils.formatCallsignRemoveSSID(arg)
        return LinkResponse.sendText("", EditSubjectApp(MailMessage(fromCallsign, toCallsign), mailboxStore))
    }

    private fun deleteMessage(request: LinkRequest): LinkResponse {
        var message: MailMessage? = null
        val userCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val messageNumber = parseIntArgument(request.message)
        if (null != messageNumber) {
            message = mailboxStore.deleteMessage(userCallsign, messageNumber)
        }
        return when (message) {
            null -> LinkResponse.sendText("No message found")
            else -> return LinkResponse.sendText("Deleted $messageNumber ${message.subject}")
        }
    }

}