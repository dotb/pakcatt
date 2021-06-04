package pakcatt.application.mailbox

import org.slf4j.LoggerFactory
import pakcatt.application.mailbox.edit.EditSubjectApp
import pakcatt.application.shared.*
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.util.StringUtils
import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.text.SimpleDateFormat

class MailboxApp(private val mailboxStore: MailboxStore): SubApp() {

    private val logger = LoggerFactory.getLogger(MailboxApp::class.java)
    private val stringUtils = StringUtils()
    private val tabSpace = "\t\t"
    private val eol = "\r\n"

    override fun returnCommandPrompt(): String {
        return "mail>"
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
        val command = parseCommand(request.message)
        return try {
            when (command.command) {
                "list" -> listMessages(request)
                "read" -> readMessage(request.remoteCallsign, command.arg)
                "send" -> sendMessage(request, command.arg)
                "del" -> deleteMessage(request.remoteCallsign, command.arg)
                "help" -> InteractionResponse.sendText("list, read, send, del, quit")
                "quit" -> InteractionResponse.sendText("Bye", NavigateBack(1))
                else -> InteractionResponse.sendText("?? - try help")
            }
        } catch (e: NumberFormatException) {
            logger.error("Argument from {} for command {} {} was not an int", request.remoteCallsign, command.command, command.arg)
            InteractionResponse.sendText("Invalid argument")
        }
    }

    private fun listMessages(request: LinkRequest): InteractionResponse {
        val userMessages = mailboxStore.messageListForCallsign(request.remoteCallsign)
        val listResponse = StringBuilder()
        val messageCount = userMessages.size
        val dateFormatter = SimpleDateFormat("dd MMM HH:mm")

        if (messageCount > 0) {
            listResponse.append(eol)
            listResponse.append("No${tabSpace}Date          From${tabSpace}To${tabSpace}Subject${eol}")
            for (message in userMessages) {
                listResponse.append(message.messageNumber)
                listResponse.append(tabSpace)
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
        listResponse.append(messageCount)
        listResponse.append(" messages")
        listResponse.append(eol)
        return InteractionResponse.sendText(listResponse.toString())
    }

    private fun readMessage(userCallsign: String, arg: String): InteractionResponse {
        val messageNumber = arg.toInt()
        return when (val message = mailboxStore.getMessage(userCallsign, messageNumber)) {
            null -> InteractionResponse.sendText("No message for $arg")
            else -> InteractionResponse.sendText("\r\nSubject: ${message.subject}\r\n${message.body.toString()}")
        }
    }

    private fun sendMessage(request: LinkRequest, arg: String): InteractionResponse {
        val fromCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val toCallsign = stringUtils.formatCallsignRemoveSSID(arg)
        return InteractionResponse.sendText("", EditSubjectApp(MailMessage(fromCallsign, toCallsign), mailboxStore))
    }

    private fun deleteMessage(userCallsign: String, arg: String): InteractionResponse {
        val messageNumber = arg.toInt()
        return when (val message = mailboxStore.deleteMessage(userCallsign, messageNumber)) {
            null -> InteractionResponse.sendText("No message for $arg")
            else -> return InteractionResponse.sendText("Deleted $messageNumber ${message.subject}")
        }
    }

    private fun parseCommand(inputLine: String): Command {
        val commandComponents = stringUtils.chompString(inputLine).split(" ")
        return if (commandComponents.size >= 2) {
            val command = commandComponents[0]
            val arg = commandComponents[1]
            Command(command, arg)
        } else {
            Command(stringUtils.chompString(inputLine), "")
        }
    }

}