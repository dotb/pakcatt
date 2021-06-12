package pakcatt.application.shared

import pakcatt.application.shared.command.Command
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.LinkResponse
import pakcatt.util.StringUtils
import java.lang.NumberFormatException
import java.lang.StringBuilder

abstract class SubApp {

    private val commands = ArrayList<Command>()
    protected val stringUtils = StringUtils()

    abstract fun returnCommandPrompt(): String

    abstract fun handleReceivedMessage(request: LinkRequest): LinkResponse

    fun registerCommand(command: Command) {
        commands.add(command)
    }

    fun handleRequestWithRegisteredCommand(request: LinkRequest): LinkResponse {
        val commandText = parseCommand(request.message)
        for (command in commands) {
            if (command.commandText() == commandText || command.shortCutText() == commandText) {
                return command.execute(request)
            }
        }
        return helpResponse()
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