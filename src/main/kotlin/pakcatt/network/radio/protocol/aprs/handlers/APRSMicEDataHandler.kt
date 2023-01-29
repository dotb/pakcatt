package pakcatt.network.radio.protocol.aprs.handlers

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pakcatt.application.shared.AppInterface
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.Location
import pakcatt.application.shared.model.ResponseType
import pakcatt.network.radio.protocol.aprs.model.*
import pakcatt.util.StringUtils

@Component
class APRSMicEDataHandler(myCall: String,
                          aprsQueue: APRSQueue,
                          appInterface: AppInterface,
                          stringUtils: StringUtils): APRSHandler(myCall, aprsQueue, appInterface, stringUtils) {

    private val logger = LoggerFactory.getLogger(APRSMicEDataHandler::class.java)

    override fun isAbleToSupport(aprsDataType: APRSDataType): Boolean {
        return listOf(APRSDataType.MIC_E,
                      APRSDataType.MIC_E_OLD,
                      APRSDataType.MIC_E_DATA,
                      APRSDataType.MIC_E_DATA_OLD).contains(aprsDataType)
    }

    override fun handleAPRSFrame(aprsFrame: APRSFrame) {
        val micEDataFrame = aprsFrame as? APRSMicEDataFrame
        if (null != micEDataFrame) {
            // Find an app that will handle this location update
            val appRequest = constructAppRequest(micEDataFrame)
            val appResponse = appInterface.getResponseForReceivedMessage(appRequest)

            // Send a response message if required
            if (appResponse.responseType == ResponseType.ACK_WITH_TEXT) {
                aprsQueue.queueAPRSMessageForDelivery(aprsFrame.channelIdentifier, myCall, micEDataFrame.sourceCallsign(), appResponse.responseString())
            }
        } else {
            logger.trace("APRS message is not for us {}", aprsFrame)
        }
    }

    private fun constructAppRequest(micEDataFrame: APRSMicEDataFrame): AppRequest {
        val remoteStationCanReceiveResponse = micEDataFrame.radioCompatibility() == RadioCompatibility.MESSAGE
        val location = Location(micEDataFrame.latitudeDegreesMinutesHundredths(), micEDataFrame.longitudeDegreesMinutesHundredthsWithAmbiguity(),
                                micEDataFrame.latitudeDecimalDegreesNorth(), micEDataFrame.longitudeDecimalDegreesEast(),
                                micEDataFrame.ambiguity(),
                                micEDataFrame.speedKmh(), micEDataFrame.speedKnots(),
                                micEDataFrame.courseDegrees())
        return AppRequest(micEDataFrame.sourceCallsign(),
                          stringUtils.formatCallsignRemoveSSID(micEDataFrame.sourceCallsign()),
                          micEDataFrame.destCallsign(),
                          micEDataFrame.statusText(),
                          false,
                          micEDataFrame.repeaterCallsignOne(),
                          micEDataFrame.repeaterCallsignTwo(),
                          remoteStationCanReceiveResponse,
                          location)
    }

}