package pakcatt.application.shared

import pakcatt.network.radio.protocol.packet.model.LinkRequest
import pakcatt.network.radio.protocol.packet.model.LinkResponse

/**
 * A simple class that represents navigation to any previous app
 */
class NavigateBack(val steps: Int): SubApp() {
    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        return LinkResponse.ignore()
    }
}