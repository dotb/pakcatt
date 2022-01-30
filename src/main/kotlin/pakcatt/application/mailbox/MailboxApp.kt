package pakcatt.application.mailbox

import pakcatt.application.bulletinboard.persistence.BulletinBoardPost
import pakcatt.application.mailbox.edit.EditSubjectApp
import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.*
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.list.LimitType
import pakcatt.application.shared.list.ListLimiter
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import java.lang.StringBuilder

class MailboxApp(private val mailboxStore: MailboxStore): SubApp() {

    init {
        registerCommand(Command("list") .function (::listMessages).description("List all messages or list unread to only see your unread messages"))
        registerCommand(Command("send") .function(::sendMessage).description("Send a message, passing the destination callsign as an argument"))
        registerCommand(Command("open") .function(::readMessage).description("Read a single message, passing the message number as an argument"))
        registerCommand(Command("del")  .function(::deleteMessage).description("Delete a message, passing the message number as an argument"))
        registerCommand(Command("back") .reply("Bye").openApp(NavigateBack(1)).description("Leave the mail app and return to the main menu"))
    }

    override fun returnCommandPrompt(): String {
        return "mail>"
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return handleRequestWithARegisteredCommand(request, parsedCommandTokens)
    }

    fun unreadMessageCount(request: AppRequest): Int {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        return mailboxStore.getUnreadMessagesTo(formattedCallsign).size
    }

    private fun listMessages(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val argUnread = parsedCommandTokens.argumentAtIndexAsString(1)
        val argLimitOne = parsedCommandTokens.argumentAtIndexAsInt(1)
        val argLimitTwo = parsedCommandTokens.argumentAtIndexAsInt(2)
        val onlyNew = argUnread == "unread"
        val userMessages = mailboxStore.messageListForCallsign(request.remoteCallsign, onlyNew)

        val listLimit = argLimitTwo ?: argLimitOne
        val listLimiter = when (listLimit) {
            null -> ListLimiter(userMessages.size, LimitType.LIST_TAIL)
            else -> ListLimiter<MailMessage>(listLimit, LimitType.LIST_TAIL)
        }
        listLimiter.addItems(userMessages)
        return compileMessageListResponse(request.channelIsInteractive, listLimiter)
    }

    private fun compileMessageListResponse(channelIsInteractive: Boolean, listLimiter: ListLimiter<MailMessage>): AppResponse {
        val listResponse = StringBuilder()
        val messageCount = listLimiter.getAllItems().size

        if (messageCount > 0) {
            if (channelIsInteractive) {
                listResponse.append(stringUtils.EOL)
                listResponse.append(textFormat.format(FORMAT.BOLD))
                listResponse.append("  No${tabSpace}Date          From${tabSpace}To${tabSpace}Subject")
                listResponse.append(textFormat.format(FORMAT.RESET))
                listResponse.append(stringUtils.EOL)
            }

            for (limitedMessage in listLimiter.getLimitedList()) {
                val message = limitedMessage.item
                listResponse.append(when (message.isRead) {
                    true -> "  "
                    false -> "* "
                })
                listResponse.append(message.messageNumber)
                if (channelIsInteractive) {
                    listResponse.append(tabSpace)
                    listResponse.append(stringUtils.formattedDateLong(message.dateTime))
                    listResponse.append("  ")
                } else {
                    listResponse.append(" ")
                    listResponse.append(stringUtils.formattedDateShort(message.dateTime))
                    listResponse.append(" ")
                }
                listResponse.append(message.fromCallsign)
                if (channelIsInteractive) {
                    listResponse.append(tabSpace)
                } else {
                    listResponse.append("->")
                }
                listResponse.append(message.toCallsign)
                if (channelIsInteractive) {
                    listResponse.append(tabSpace)
                } else {
                    listResponse.append(": ")
                }
                listResponse.append(message.subject)
                listResponse.append(stringUtils.EOL)
            }
        }
        if (channelIsInteractive) {
            listResponse.append(messageCount)
            listResponse.append(" messages")
            listResponse.append(stringUtils.EOL)
        }
        return AppResponse.sendText(listResponse.toString())
    }

    private fun readMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val userCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        var message: MailMessage? = null
        val messageNumber = parsedCommandTokens.argumentAtIndexAsInt(1)
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

    private fun sendMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val arg = parsedCommandTokens.argumentAtIndexAsString(1)
        val fromCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val toCallsign = stringUtils.formatCallsignRemoveSSID(arg)
        return AppResponse.sendText("", EditSubjectApp(MailMessage(fromCallsign, toCallsign), mailboxStore))
    }

    private fun deleteMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        var message: MailMessage? = null
        val userCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val messageNumber = parsedCommandTokens.argumentAtIndexAsInt(1)
        if (null != messageNumber) {
            message = mailboxStore.deleteMessage(userCallsign, messageNumber)
        }
        return when (message) {
            null -> AppResponse.sendText("No message found")
            else -> return AppResponse.sendText("Deleted $messageNumber ${message.subject}")
        }
    }

}