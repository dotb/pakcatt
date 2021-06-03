package pakcatt.application.mailbox

import pakcatt.application.shared.*
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.util.StringUtils
import java.lang.StringBuilder
import java.text.SimpleDateFormat

class MailboxApp(private val mailboxStore: MailboxStore): SubApp() {

    private var editingInProgress = HashMap<String, EditState>()
    private val stringUtils = StringUtils()
    private val tabSpace = "\t\t"
    private val eol = "\r\n"

    override fun returnCommandPrompt(): String {
        return "mail>"
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
        // Check if a message is being edited. Otherwise look for any mail commands.
        return when {
            editingInProgress.containsKey(request.remoteCallsign) -> handleMessageInput(request)
            else -> handleMailCommand(request)
        }
    }

    fun handleMessageInput(request: LinkRequest): InteractionResponse {
        val editState = editStateForCallsign(request.remoteCallsign)
        return when (editState.state) {
            EditState.MessageEditState.EDITING_SUBJECT -> addSubjectToMessage(editState, request.message)
            EditState.MessageEditState.EDITING_MESSAGE -> addBodyToMessage(editState, request.message)
        }
    }

    fun handleMailCommand(request: LinkRequest): InteractionResponse {
        val command = parseCommand(request.message)
        return when (command.command) {
            "list" -> listMessages(request)
            "read" -> readMessage(command.arg)
            "send" -> sendMessage(request, command.arg)
            "del" -> deleteMessage(command.arg)
            "help" -> InteractionResponse.sendText("list, read, send, del, quit")
            "quit" -> InteractionResponse.sendText("Bye", NavigateBack())
            else -> InteractionResponse.sendText("?? - try help")
        }
    }

    fun listMessages(request: LinkRequest): InteractionResponse {
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

    fun readMessage(arg: String): InteractionResponse {
        return InteractionResponse.ignore()
    }

    fun sendMessage(request: LinkRequest, arg: String): InteractionResponse {
        val editState = editStateForCallsign(request.remoteCallsign.toUpperCase())
        editState.message.toCallsign = arg.toUpperCase()
        return InteractionResponse.sendText("Subject:")
    }

    fun deleteMessage(arg: String): InteractionResponse {
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

    private fun editStateForCallsign(userCallsign: String): EditState {
        var editState = editingInProgress[userCallsign]
        if (null == editState) {
            editState = EditState(userCallsign)
            editingInProgress.put(userCallsign, editState)
        }
        return editState
    }

    private fun addSubjectToMessage(editState: EditState, subject: String): InteractionResponse {
        editState.message.subject = stringUtils.chompString(subject)
        editState.state = EditState.MessageEditState.EDITING_MESSAGE
        return InteractionResponse.sendText("Thanks. Now compose your message and finish with . on a line of it's own.")
    }

    private fun addBodyToMessage(editState: EditState, body: String): InteractionResponse {
        val chompedBody = stringUtils.chompString(body)
        return if (chompedBody == ".") {
            mailboxStore.storeMessage(editState.message)
            editingInProgress.remove(editState.userCallsign)
            InteractionResponse.sendText("Thanks! Your message has been stored.")
        } else {
            editState.message.message.append(chompedBody)
            editState.message.message.append(eol)
            InteractionResponse.acknowledgeOnly()
        }
    }

}