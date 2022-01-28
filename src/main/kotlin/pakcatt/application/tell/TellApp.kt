package pakcatt.application.tell

import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.network.radio.protocol.aprs.model.APRSMessageFrame
import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens

/**
 * Allows users to send quick messages to others
 */
class TellApp(private val destinationCallsign: String, private val myCallsign: String, private val senderCallsign: String): SubApp() {

    private val messagePrefix = "${stringUtils.formatCallsignRemoveSSID(senderCallsign)} says:"

    override fun returnCommandPrompt(): String {
        val maxContentLength = APRSMessageFrame.MAX_CONTENT_LENGTH - messagePrefix.length
        return "Message? (max $maxContentLength):"
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val message = "$messagePrefix ${stringUtils.removeEOLChars(request.message)}"
        queueAdhocMessageForTransmission(destinationCallsign, myCallsign, message, DeliveryType.APRS_FIRE_AND_FORGET)
        return AppResponse.sendText("Thanks, I'll send that right away!", NavigateBack(1))
    }

}