package pakcatt.application.mailbox

import pakcatt.application.mailbox.edit.EditSubjectApp
import pakcatt.application.shared.*
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.util.StringUtils
import java.lang.StringBuilder
import java.text.SimpleDateFormat

class MailboxApp(private val mailboxStore: MailboxStore): SubApp() {

    private val stringUtils = StringUtils()
    private val tabSpace = "\t\t"
    private val eol = "\r\n"

    override fun returnCommandPrompt(): String {
        return "mail>"
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
        val command = parseCommand(request.message)
        return when (command.command) {
            "list" -> listMessages(request)
            "read" -> readMessage(command.arg)
            "send" -> sendMessage(request, command.arg)
            "del" -> deleteMessage(command.arg)
            "help" -> InteractionResponse.sendText("list, read, send, del, quit")
            "quit" -> InteractionResponse.sendText("Bye", NavigateBack(1))
            else -> InteractionResponse.sendText("?? - try help")
        }
    }

    private fun listMessages(request: LinkRequest): InteractionResponse {
        val userMessages = mailboxStore.messagesForCallsign(request.remoteCallsign)
        val listResponse = StringBuilder()
        val messageCount = userMessages.size
        val dateFormatter = SimpleDateFormat("dd MMM HH:mm")
        listResponse.append(messageCount)
        listResponse.append(" messages")
        listResponse.append(eol)
        if (messageCount > 0) {
            listResponse.append("Date          From${tabSpace}To${tabSpace}Subject${eol}")
            for (message in userMessages) {
                listResponse.append(dateFormatter.format(message.dateTime.time))
                listResponse.append("  ")
                listResponse.append(message.fromCallsign)
                listResponse.append(tabSpace)
                listResponse.append(message.toCallsign)
                listResponse.append(tabSpace)
                listResponse.append(message.subject)
                listResponse.append(eol)
            }
        }
        return InteractionResponse.sendText(listResponse.toString())
    }

    private fun readMessage(arg: String): InteractionResponse {
        return InteractionResponse.ignore()
    }

    private fun sendMessage(request: LinkRequest, arg: String): InteractionResponse {
        return InteractionResponse.sendText("", EditSubjectApp(MailMessage(request.remoteCallsign, arg), mailboxStore))
    }

    private fun deleteMessage(arg: String): InteractionResponse {
        return InteractionResponse.ignore()
    }

    private fun parseCommand(inputLine: String): Command {
        val commandComponents = stringUtils.chompString(inputLine).split(" ")
        if (commandComponents.size >= 2) {
            val command = commandComponents[0]
            val arg = commandComponents[1]
            return Command(command, arg)
        } else {
            return Command(stringUtils.chompString(inputLine), "")
        }
    }

}