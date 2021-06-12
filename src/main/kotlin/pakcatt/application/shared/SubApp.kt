package pakcatt.application.shared

import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.LinkResponse

abstract class SubApp {

    abstract fun returnCommandPrompt(): String

    abstract fun handleReceivedMessage(request: LinkRequest): LinkResponse

}