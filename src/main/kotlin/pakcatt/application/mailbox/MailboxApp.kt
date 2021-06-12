package pakcatt.application.mailbox

import org.slf4j.LoggerFactory
import pakcatt.application.mailbox.edit.EditSubjectApp
import pakcatt.application.mailbox.persistence.MailMessage
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.*
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.LinkResponse
import pakcatt.util.StringUtils
import java.lang.NumberFormatException
import java.lang.StringBuilder
import java.text.SimpleDateFormat

class MailboxApp(private val mailboxStore: MailboxStore): SubApp() {

    private val logger = LoggerFactory.getLogger(MailboxApp::class.java)
    private val stringUtils = StringUtils()
    private val tabSpace = "\t"
    private val eol = "\n\r"

    override fun returnCommandPrompt(): String {
        return "mail>"
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        val command = parseCommand(request.message)
        return try {
            when (command.command) {
                "list" -> listMessages(request)
                "read" -> readMessage(request.remoteCallsign, command.arg)
                "send" -> sendMessage(request, command.arg)
                "del" -> deleteMessage(request.remoteCallsign, command.arg)
                "quit" -> LinkResponse.sendText("Bye", NavigateBack(1))
                else -> LinkResponse.sendText("Options are: list, read, send, del, quit")
            }
        } catch (e: NumberFormatException) {
            logger.error("Argument from {} for command {} {} was not an int", request.remoteCallsign, command.command, command.arg)
            LinkResponse.sendText("Invalid argument")
        }
    }

    private fun listMessages(request: LinkRequest): LinkResponse {
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
        return LinkResponse.sendText(listResponse.toString())
    }

    private fun readMessage(userCallsign: String, arg: String): LinkResponse {
        val messageNumber = arg.toInt()
        return when (val message = mailboxStore.getMessage(userCallsign, messageNumber)) {
            null -> LinkResponse.sendText("No message for $arg")
            else -> LinkResponse.sendText("${eol}Subject: ${message.subject}${eol}${message.body.toString()}")
        }
    }

    private fun sendMessage(request: LinkRequest, arg: String): LinkResponse {
        val fromCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val toCallsign = stringUtils.formatCallsignRemoveSSID(arg)
        return LinkResponse.sendText("", EditSubjectApp(MailMessage(fromCallsign, toCallsign), mailboxStore))
    }

    private fun deleteMessage(userCallsign: String, arg: String): LinkResponse {
        val messageNumber = arg.toInt()
        return when (val message = mailboxStore.deleteMessage(userCallsign, messageNumber)) {
            null -> LinkResponse.sendText("No message for $arg")
            else -> return LinkResponse.sendText("Deleted $messageNumber ${message.subject}")
        }
    }

    private fun parseCommand(inputLine: String): Command {
        val commandComponents = stringUtils.removeEOLChars(inputLine).split(" ")
        return if (commandComponents.size >= 2) {
            val command = commandComponents[0]
            val arg = commandComponents[1]
            Command(command, arg)
        } else {
            Command(stringUtils.removeEOLChars(inputLine), "")
        }
    }

}