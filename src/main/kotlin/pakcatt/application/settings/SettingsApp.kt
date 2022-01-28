package pakcatt.application.settings

import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import pakcatt.util.StringUtils

class SettingsApp: SubApp() {

    init {
        registerCommand(Command("list") .function(::listSettings).description("List settings"))
        registerCommand(Command("back") .reply("Bye").openApp(NavigateBack(1)).description("Return to the main menu"))
    }

    override fun returnCommandPrompt(): String {
        return "settings>"
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return handleRequestWithARegisteredCommand(request, parsedCommandTokens)
    }

    private fun listSettings(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val userContext = request.userContext
        return if (null != userContext) {
            val userEOLConfig = when (userContext.eolSequence) {
                StringUtils.CRLF -> "CRLF"
                StringUtils.LFCR -> "LFCR"
                StringUtils.CR.toString() -> "CR"
                else -> "LF"
            }
            AppResponse.sendText("Your terminal EOL is set to $userEOLConfig")
        } else {
            AppResponse.sendText("No configuration set")
        }
    }

}