package pakcatt.application.shared

import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse

/**
 * A simple class that represents navigation to any previous app
 */
class NavigateBack(val steps: Int): SubApp() {
    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        return AppResponse.ignore()
    }
}