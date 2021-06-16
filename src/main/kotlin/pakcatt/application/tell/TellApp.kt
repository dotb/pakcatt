package pakcatt.application.tell

import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.network.packet.protocol.no_layer_three.model.DeliveryType
import pakcatt.network.packet.protocol.no_layer_three.model.LinkRequest
import pakcatt.network.packet.protocol.no_layer_three.model.LinkResponse

/**
 * Allows users to send quick messages to others
 */
class TellApp(private val destinationCallsign: String, private val myCallsign: String): SubApp() {

    override fun returnCommandPrompt(): String {
        return "Message? (max 67):"
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        val message = stringUtils.removeEOLChars(request.message)
        queueAdhocMessageForTransmission(destinationCallsign, myCallsign, message, DeliveryType.APRS_FIRE_AND_FORGET)
        return LinkResponse.sendText("Thanks, I'll send that right away!", NavigateBack(1))
    }

}