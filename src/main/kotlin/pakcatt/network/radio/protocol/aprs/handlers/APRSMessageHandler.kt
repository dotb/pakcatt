package pakcatt.network.radio.protocol.aprs.handlers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pakcatt.application.shared.AppInterface
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.ResponseType
import pakcatt.network.radio.protocol.aprs.model.APRSDataType
import pakcatt.network.radio.protocol.aprs.model.APRSFrame
import pakcatt.network.radio.protocol.aprs.model.APRSMessageFrame
import pakcatt.network.radio.protocol.aprs.model.APRSQueue
import pakcatt.util.StringUtils

@Component
class APRSMessageHandler(myCall: String,
                         aprsQueue: APRSQueue,
                         appInterface: AppInterface,
                         stringUtils: StringUtils): APRSHandler(myCall, aprsQueue, appInterface, stringUtils) {

    private val logger = LoggerFactory.getLogger(APRSMessageHandler::class.java)

    override fun isAbleToSupport(aprsDataType: APRSDataType): Boolean {
        return aprsDataType == APRSDataType.MESSAGE
    }

    override fun handleAPRSFrame(aprsFrame: APRSFrame) {
        val aprsMessageFrame = aprsFrame as? APRSMessageFrame
        if (null != aprsMessageFrame &&
            aprsMessageFrame.messageDestinationCallsign() == stringUtils.formatCallsignEnsureSSID(myCall)) {

                // Find an app that will handle this message
                val appRequest = AppRequest(aprsFrame.channelIdentifier,
                                            aprsMessageFrame.messageSourceCallsign(),
                                            stringUtils.formatCallsignRemoveSSID(aprsMessageFrame.messageSourceCallsign()),
                                            aprsMessageFrame.messageDestinationCallsign(),
                                            aprsMessageFrame.message(),
                                            aprsFrame.repeaterCallsignOne(),
                                            aprsFrame.repeaterCallsignTwo(),
                                            true)
                val appResponse = appInterface.getResponseForReceivedMessage(appRequest)

                // Send an acknowledgement message if a message number was sent to us
                if (aprsMessageFrame.messageNumber() > 0 &&
                    (appResponse.responseType == ResponseType.ACK_ONLY ||
                            appResponse.responseType == ResponseType.ACK_WITH_TEXT)) {
                    // I've found that the FT3D wants the ACK number have a prepended 0 when less than 10. For example, "08" and not "8"
                    var ackMessage = "ack${aprsMessageFrame.messageNumber()}"
                    if (aprsFrame.messageNumber() < 10) {
                        ackMessage = "ack0${aprsMessageFrame.messageNumber()}"
                    }
                    aprsQueue.queueAPRSMessageForDelivery(aprsFrame.channelIdentifier, myCall, aprsMessageFrame.messageSourceCallsign(), ackMessage)
                }

                // Send a response message if required
                if (appResponse.responseType == ResponseType.ACK_WITH_TEXT) {
                    aprsQueue.queueAPRSMessageForDelivery(aprsFrame.channelIdentifier, myCall, aprsMessageFrame.messageSourceCallsign(), appResponse.responseString())
                }

        } else {
            logger.trace("APRS message is not for us {}", aprsFrame)
        }
    }

}