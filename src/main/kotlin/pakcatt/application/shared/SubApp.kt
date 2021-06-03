package pakcatt.application.shared

import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.network.packet.link.model.LinkRequest

abstract class SubApp {

    abstract fun returnCommandPrompt(): String

    abstract fun handleReceivedMessage(request: LinkRequest): InteractionResponse

}