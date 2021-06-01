package pakcatt.application.mailbox

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.shared.AppRequest
import pakcatt.application.shared.ConnectionResponse
import pakcatt.application.shared.InteractionResponse
import pakcatt.application.shared.PakCattApp
import pakcatt.util.StringUtils
import java.lang.StringBuilder
import java.text.SimpleDateFormat

@Component
@Profile("production")
class MailboxApp(val myCall: String,
                 val mailboxStore: MailboxStore): PakCattApp() {

    private var editingInProgress = HashMap<String, EditState>()
    private val stringUtils = StringUtils()
    private val tabSpace = "\t\t"
    private val eol = "\r\n"

    override fun decisionOnConnectionRequest(request: AppRequest): ConnectionResponse {
        return when (isAddressedToMe(request, myCall)) {
            true -> ConnectionResponse.connectWithMessage("Welcome to PakCatt! Type help to learn more :-)")
            false -> ConnectionResponse.ignore()
        }
    }

    override fun handleReceivedMessage(request: AppRequest): InteractionResponse {
        // Check if a message is being edited. Otherwise look for any mail commands.
        return when {
            editingInProgress.containsKey(request.remoteCallsign) -> handleMessageInput(request)
            else -> handleMailCommand(request)
        }
    }

    fun handleMessageInput(request: AppRequest): InteractionResponse {
        val editState = editStateForCallsign(request.remoteCallsign)
        return when (editState.state) {
            EditState.MessageEditState.EDITING_SUBJECT -> addSubjectToMessage(editState, request.message)
            EditState.MessageEditState.EDITING_MESSAGE -> addBodyToMessage(editState, request.message)
        }
    }

    fun handleMailCommand(request: AppRequest): InteractionResponse {
        val command = parseCommand(request.message)
        return when (command.command) {
            "list" -> listMessages(request)
            "read" -> readMessage(command.arg)
            "send" -> sendMessage(request, command.arg)
            "del" -> deleteMessage(command.arg)
            else -> InteractionResponse.ignore()
        }
    }

    fun listMessages(request: AppRequest): InteractionResponse {
        val userMessages = mailboxStore.messagesForCallsign(request.remoteCallsign)
        val listResponse = StringBuilder()
        val messageCount = userMessages.size
        val dateFormatter = SimpleDateFormat("dd/MM/yy hh:mm a")
        listResponse.append(messageCount)
        listResponse.append(" messages")
        listResponse.append(eol)
        if (messageCount > 0) {
            listResponse.append("Date $tabSpace From $tabSpace To $tabSpace Subject $eol")
            for (message in userMessages) {
                listResponse.append(dateFormatter.format(message.dateTime.time))
                listResponse.append(tabSpace)
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

    fun sendMessage(request: AppRequest, arg: String): InteractionResponse {
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
            InteractionResponse.acknowlegeOnly()
        }
    }

}