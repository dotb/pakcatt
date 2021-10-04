package pakcatt.application.settings

import pakcatt.application.shared.FORMAT
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.util.StringUtils
import java.lang.StringBuilder

class SettingsApp: SubApp() {

    init {
        registerCommand(Command("list") .function { listSettings(it) }  .description("List settings"))
        registerCommand(Command("back") .reply("Bye").openApp(NavigateBack(1)).description("Return to the main menu"))
        registerCommand(Command("set")) .function { setSetting(it) } .description("Make setting")
    }

    override fun returnCommandPrompt(): String {
        return "settings>"
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        return handleRequestWithRegisteredCommand(request)
    }

    private fun listSettings(request: AppRequest): AppResponse {
        val userContext = request.userContext
        return if (null != userContext) {
            AppResponse.sendText(compileListOfSettings(userContext))
        } else {
            AppResponse.sendText("No configuration set")
        }
    }

    private fun compileListOfSettings(request: AppRequest): String {
        val userContext = request.userContext
        val listOfSettings = listOf(userContext.eolSequence)

        // Want this to be a list of functions that provides the outputs
        val listOfSettingsInterpreters = listOf(this::settingsOutputEOL)

        // Heading for rows to be outputted below
        val settingResponse = StringBuilder()
        settingResponse.append(stringUtils.EOL)
        settingResponse.append(textFormat.format(FORMAT.BOLD))
        settingResponse.append("Setting Name${tabSpace}Is User-configurable?${tabSpace}Setting Value")
        settingResponse.append(textFormat.format(FORMAT.RESET))
        settingResponse.append(stringUtils.EOL)

        for(setting in listOfSettings) {
            // Print the setting

        }
    }

    private fun settingsOutputEOL(request: AppRequest): String {
        // Output the users's EOL preferences
        val userContext = request.userContext
        val userEOLConfig = when (userContext.eolSequence) {
            StringUtils.CRLF -> "CRLF"
            StringUtils.LFCR -> "LFCR"
            StringUtils.CR.toString() -> "CR"
            else -> "LF"
        }
        return "EOL${tabSpace}$Y${tabSpace}userEOLConfig"
    }

    private fun setSetting(request: AppRequest): AppResponse {
        // Get key and value to be set
        val settingName = parseArgument(request.message)[1]
        val settingValue = parseArgument(request.message)[2]

        // Put these in the database

        // Set these in the user context, confirming database insert was successful beforehand
    }

}