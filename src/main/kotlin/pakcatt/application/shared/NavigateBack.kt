package pakcatt.application.shared

import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens

/**
 * A simple class that represents navigation to any previous app
 */
class NavigateBack(val steps: Int): SubApp() {
    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return AppResponse.ignore()
    }
}