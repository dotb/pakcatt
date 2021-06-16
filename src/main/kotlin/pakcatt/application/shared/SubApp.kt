package pakcatt.application.shared

import pakcatt.application.shared.command.Command
import pakcatt.network.radio.protocol.packet.model.DeliveryType
import pakcatt.network.radio.protocol.packet.model.LinkRequest
import pakcatt.network.radio.protocol.packet.model.LinkResponse
import pakcatt.util.StringUtils
import java.lang.NumberFormatException
import java.lang.StringBuilder

abstract class SubApp {

    private val commands = ArrayList<Command>()
    protected val stringUtils = StringUtils()
    private var parentRootApp: RootApp? = null

    abstract fun returnCommandPrompt(): String

    abstract fun handleReceivedMessage(request: LinkRequest): LinkResponse

    fun registerCommand(command: Command) {
        commands.add(command)
    }

    fun handleRequestWithRegisteredCommand(request: LinkRequest): LinkResponse {
        val commandText = parseCommand(request.message)
        if (commandText.isEmpty()) {
            // An empty request should return only the prompt / new line
            return LinkResponse.sendText("")
        } else {
            // Find a command that handles the command sent to us
            for (command in commands) {
                if (command.commandText() == commandText
                    || (command.shortCutText().isNotEmpty() && command.shortCutText() == commandText)
                ) {
                    return command.execute(request)
                }
            }
            return helpResponse()
        }
    }

    open fun queueAdhocMessageForTransmission(remoteCallsign: String,
                                         myCallsign: String,
                                         message: String,
                                         deliveryType: DeliveryType
    ) {
        parentRootApp?.queueAdhocMessageForTransmission(remoteCallsign, myCallsign, message, deliveryType)
    }

    fun setParentRootApp(parent: RootApp?) {
        this.parentRootApp = parent
    }

    protected fun parseCommand(inputLine: String): String {
        val stringTokens = inputLine.split(" ")
        return when (stringTokens.size) {
            2 -> stringUtils.removeEOLChars(stringTokens[0]).toLowerCase()
            else -> stringUtils.removeEOLChars(inputLine).toLowerCase()
        }
    }

    protected fun parseStringArgument(inputLine: String, defaultArgument: String): String {
        val stringTokens = inputLine.split(" ")
        return when (stringTokens.size) {
            2 -> stringUtils.removeEOLChars(stringTokens[1]).toLowerCase()
            else -> defaultArgument.toLowerCase()
        }
    }

    protected fun parseIntArgument(inputLine: String): Int? {
        val stringArg = parseStringArgument(inputLine, "")
        return try {
            stringArg.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun helpResponse(): LinkResponse {
        val stringBuilder = StringBuilder()
        stringBuilder.append("\r\n")
        for (command in commands) {
            val myDescription = command.descriptionText()
            if (null != myDescription) {
                stringBuilder.append(command.commandText())
                stringBuilder.append("\t- ")
                stringBuilder.append(myDescription)
                stringBuilder.append("\r\n")
            }
        }
        stringBuilder.append("help")
        stringBuilder.append("\t- ")
        stringBuilder.append("Display this list of commands")
        stringBuilder.append("\r\n")
        return LinkResponse.sendText(stringBuilder.toString())
    }

}