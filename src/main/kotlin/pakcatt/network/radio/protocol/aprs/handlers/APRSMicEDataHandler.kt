package pakcatt.network.radio.protocol.aprs.handlers

import org.slf4j.LoggerFactory
import pakcatt.application.shared.AppInterface
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.Location
import pakcatt.application.shared.model.ResponseType
import pakcatt.network.radio.protocol.aprs.model.APRSFrame
import pakcatt.network.radio.protocol.aprs.model.APRSMicEDataFrame
import pakcatt.network.radio.protocol.aprs.model.APRSQueue
import pakcatt.util.StringUtils

class APRSMicEDataHandler(myCall: String,
                          aprsQueue: APRSQueue,
                          appInterface: AppInterface,
                          stringUtils: StringUtils): APRSHandler(myCall, aprsQueue, appInterface, stringUtils) {

    private val logger = LoggerFactory.getLogger(APRSMicEDataHandler::class.java)

    override fun handleAPRSFrame(aprsFrame: APRSFrame) {
        val micEDataFrame = aprsFrame as? APRSMicEDataFrame
        if (null != micEDataFrame) {
            // Find an app that will handle this location update
            val appRequest = constructAppRequest(micEDataFrame)
            val appResponse = appInterface.getResponseForReceivedMessage(appRequest)

            // Send a response message if required
            if (appResponse.responseType == ResponseType.ACK_WITH_TEXT) {
                aprsQueue.queueAPRSMessageForDelivery(myCall, micEDataFrame.sourceCallsign(), appResponse.responseString())
            }
        } else {
            logger.trace("APRS message is not for us {}", aprsFrame)
        }
    }

    private fun constructAppRequest(micEDataFrame: APRSMicEDataFrame): AppRequest {
        val location = Location(micEDataFrame.latitudeDegreesMinutesHundredths(), micEDataFrame.longitudeDegreesMinutesHundredthsWithAmbiguity(), micEDataFrame.ambiguity(), micEDataFrame.speedKmh(), micEDataFrame.speedKnots(), micEDataFrame.courseDegrees())
        return AppRequest(micEDataFrame.sourceCallsign(), micEDataFrame.destCallsign(), micEDataFrame.statusText(), location)
    }

}