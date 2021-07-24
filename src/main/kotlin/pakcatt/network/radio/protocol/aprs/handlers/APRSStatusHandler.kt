package pakcatt.network.radio.protocol.aprs.handlers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pakcatt.application.shared.AppInterface
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.ResponseType
import pakcatt.network.radio.protocol.aprs.model.APRSDataType
import pakcatt.network.radio.protocol.aprs.model.APRSFrame
import pakcatt.network.radio.protocol.aprs.model.APRSQueue
import pakcatt.network.radio.protocol.aprs.model.APRSStatusFrame
import pakcatt.util.StringUtils

@Component
class APRSStatusHandler(myCall: String,
                        aprsQueue: APRSQueue,
                        appInterface: AppInterface,
                        stringUtils: StringUtils): APRSHandler(myCall, aprsQueue, appInterface, stringUtils) {

    private val logger = LoggerFactory.getLogger(APRSStatusHandler::class.java)

    override fun isAbleToSupport(aprsDataType: APRSDataType): Boolean {
        return aprsDataType == APRSDataType.STATUS
    }

    override fun handleAPRSFrame(aprsFrame: APRSFrame) {
        val aprsStatusFrame = aprsFrame as? APRSStatusFrame
        if (null != aprsStatusFrame) {
            // Find an app that will handle this message
            val appRequest = AppRequest(aprsStatusFrame.sourceCallsign(), aprsStatusFrame.destCallsign(), aprsStatusFrame.statusText())
            val appResponse = appInterface.getResponseForReceivedMessage(appRequest)

            // Send a response message if required
            if (appResponse.responseType == ResponseType.ACK_WITH_TEXT) {
                aprsQueue.queueAPRSMessageForDelivery(myCall, aprsStatusFrame.sourceCallsign(), appResponse.responseString())
            }
        } else {
            logger.trace("APRS status frame was not typed correctly: {}", aprsFrame)
        }
    }

}