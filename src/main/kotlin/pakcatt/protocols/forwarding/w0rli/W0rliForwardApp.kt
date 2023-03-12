package pakcatt.protocols.forwarding.w0rli

import pakcatt.application.shared.SubApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens

class W0rliForwardApp: SubApp() {
    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return when (parsedCommandTokens.argumentAtIndexAsString(0)) {
        "SP" -> AppResponse.sendText("OK")
            else -> AppResponse.sendText("> ")
        }
    }
}