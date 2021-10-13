package pakcatt.application.mailbox

import pakcatt.application.mailbox.edit.EditSubjectApp
import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.*
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import java.lang.StringBuilder

class MailboxApp(private val mailboxStore: MailboxStore): SubApp() {

    init {
        registerCommand(Command("list") .function { listMessages(it) }  .description("List all messages or list unread to only see your unread messages"))
        registerCommand(Command("send") .function { sendMessage(it) }   .description("Send a message, passing the destination callsign as an argument"))
        registerCommand(Command("open") .function { readMessage(it) }   .description("Read a single message, passing the message number as an argument"))
        registerCommand(Command("del")  .function { deleteMessage(it) } .description("Delete a message, passing the message number as an argument"))
        registerCommand(Command("back") .reply("Bye").openApp(NavigateBack(1)).description("Leave the mail app and return to the main menu"))
    }

    override fun returnCommandPrompt(): String {
        return "mail>"
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        return handleRequestWithRegisteredCommand(request)
    }

    fun unreadMessageCount(request: AppRequest): Int {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        return mailboxStore.getUnreadMessagesTo(formattedCallsign).size
    }

    private fun listMessages(request: AppRequest): AppResponse {
        val arg = parseStringArgument(request.message, "")
        val onlyNew = arg == "unread"
        val userMessages = mailboxStore.messageListForCallsign(request.remoteCallsign, onlyNew)
        val listResponse = StringBuilder()
        val messageCount = userMessages.size

        if (messageCount > 0) {
            listResponse.append(stringUtils.EOL)
            listResponse.append(textFormat.format(FORMAT.BOLD))
            listResponse.append("  No${tabSpace}Date          From${tabSpace}To${tabSpace}Subject")
            listResponse.append(textFormat.format(FORMAT.RESET))
            listResponse.append(stringUtils.EOL)
            for (message in userMessages) {
                listResponse.append(when (message.isRead) {
                    true -> "  "
                    false -> "* "
                })
                listResponse.append(message.messageNumber)
                listResponse.append(tabSpace)
                listResponse.append(stringUtils.formattedDate(message.dateTime))
                listResponse.append("  ")
                listResponse.append(message.fromCallsign)
                listResponse.append(tabSpace)
                listResponse.append(message.toCallsign)
                listResponse.append(tabSpace)
                listResponse.append(message.subject)
                listResponse.append(stringUtils.EOL)
            }
        }
        listResponse.append(messageCount)
        listResponse.append(" messages")
        listResponse.append(stringUtils.EOL)
        return AppResponse.sendText(listResponse.toString())
    }

    private fun readMessage(request: AppRequest): AppResponse {
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
            AppResponse.sendText("${stringUtils.EOL}Subject: ${message.subject}${stringUtils.EOL}${message.body.toString()}")
        } else {
            AppResponse.sendText("No message found")
        }
    }

    private fun sendMessage(request: AppRequest): AppResponse {
        val arg = parseStringArgument(request.message, "")
        val fromCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val toCallsign = stringUtils.formatCallsignRemoveSSID(arg)
        return AppResponse.sendText("", EditSubjectApp(MailMessage(fromCallsign, toCallsign), mailboxStore))
    }

    private fun deleteMessage(request: AppRequest): AppResponse {
        var message: MailMessage? = null
        val userCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val messageNumber = parseIntArgument(request.message)
        if (null != messageNumber) {
            message = mailboxStore.deleteMessage(userCallsign, messageNumber)
        }
        return when (message) {
            null -> AppResponse.sendText("No message found")
            else -> return AppResponse.sendText("Deleted $messageNumber ${message.subject}")
        }
    }

}