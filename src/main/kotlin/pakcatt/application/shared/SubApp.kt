package pakcatt.application.shared

import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.util.StringUtils
import java.lang.NumberFormatException
import java.lang.StringBuilder

abstract class SubApp {

    private var parentRootApp: RootApp? = null
    protected val stringUtils = StringUtils()
    protected val textFormat = TextFormat()
    protected val tabSpace = "\t"
    protected val beepChar = 7.toChar()

    private val commands = mutableListOf(Command("help") .function { helpResponse() }  .description("Display this list of commands"))

    abstract fun returnCommandPrompt(): String

    abstract fun handleReceivedMessage(request: AppRequest): AppResponse

    fun registerCommand(command: Command) {
        commands.add(command)
    }

    fun handleRequestWithRegisteredCommand(request: AppRequest): AppResponse {
        val commandText = parseCommand(request.message)
        if (commandText.isEmpty()) {
            // An empty request should return only the prompt / new line
            return AppResponse.sendText("")
        } else {
            // Find a command that handles the request sent to us
            for (command in commands) {
                if (command.commandText() == commandText
                    || (command.shortCutText().isNotEmpty() && command.shortCutText() == commandText)
                ) {
                    return command.execute(request)
                }
            }
            return AppResponse.sendText("Say what? Type help for, help")
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

    private fun helpResponse(): AppResponse {
        val stringBuilder = StringBuilder()
        stringBuilder.append(stringUtils.EOL)
        for (command in commands) {
            val myDescription = command.descriptionText()
            if (null != myDescription) {
                stringBuilder.append(command.commandText())
                stringBuilder.append("\t- ")
                stringBuilder.append(myDescription)
                stringBuilder.append(stringUtils.EOL)
            }
        }
        return AppResponse.sendText(stringBuilder.toString())
    }

}