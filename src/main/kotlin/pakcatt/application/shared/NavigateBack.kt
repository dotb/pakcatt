package pakcatt.application.shared

import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.network.packet.link.model.LinkRequest

/**
 * A simple class that represents navigation to any previous app
 */
class NavigateBack(val steps: Int): SubApp() {
    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
        return InteractionResponse.ignore()
    }
}