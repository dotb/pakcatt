package pakcatt.application.shared

import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import pakcatt.util.StringUtils
import java.lang.StringBuilder

abstract class SubApp {

    private var parentRootApp: RootApp? = null
    protected val stringUtils = StringUtils()
    protected val textFormat = TextFormat()
    protected val tabSpace = "\t"
    protected val beepChar = 7.toChar()

    private val allRegisteredCommands = mutableListOf(Command("help") .function(::helpResponse).description("Display this list of commands"))

    abstract fun returnCommandPrompt(): String

    abstract fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse

    fun registerCommand(command: Command) {
        allRegisteredCommands.add(command)
    }

    fun handleRequestWithARegisteredCommand(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        if (parsedCommandTokens.command().isEmpty()) {
            // An empty request should return only the prompt / new line
            return AppResponse.sendText("")
        } else {
            // Find a command that handles the request sent to us
            for (registeredCommand in allRegisteredCommands) {
                if (registeredCommand.commandText() == parsedCommandTokens.command()
                    || (registeredCommand.shortCutText().contains(parsedCommandTokens.command()))) {
                    return registeredCommand.execute(request, parsedCommandTokens)
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

    private fun helpResponse(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
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