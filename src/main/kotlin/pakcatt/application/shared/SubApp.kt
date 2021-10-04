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

    private val allRegisteredCommands = mutableListOf(Command("help") .function { helpResponse() }  .description("Display this list of commands"))

    abstract fun returnCommandPrompt(): String

    abstract fun handleReceivedMessage(request: AppRequest): AppResponse

    fun registerCommand(command: Command) {
        allRegisteredCommands.add(command)
    }

    fun handleRequestWithRegisteredCommand(request: AppRequest): AppResponse {
        val userCommandInput = parseCommand(request.message)
        if (userCommandInput.isEmpty()) {
            // An empty request should return only the prompt / new line
            return AppResponse.sendText("")
        } else {
            // Find a command that handles the request sent to us
            for (registeredCommand in allRegisteredCommands) {
                if (registeredCommand.commandText() == userCommandInput
                    || (registeredCommand.shortCutText().contains(userCommandInput))
                ) {
                    return registeredCommand.execute(request)
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

    protected fun parseArgument(inputLine: String, defaultArgument: String): List<String> {
        val tokenList = inputLine.split(" ")
        return if (tokenList.isEmpty()) {
             listOf(defaultArgument.toLowerCase())
        } else {
            tokenList
        }
    }

    protected fun parseIntArgument(inputLine: String): Int? {
        val stringArg = parseArgument(inputLine, "").first()
        return try {
            stringArg.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    private fun helpResponse(): AppResponse {
        val stringBuilder = StringBuilder()
        stringBuilder.append(stringUtils.EOL)
        for (command in allRegisteredCommands) {
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