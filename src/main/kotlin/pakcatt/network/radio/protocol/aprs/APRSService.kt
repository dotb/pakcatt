package pakcatt.network.radio.protocol.aprs

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.AppInterface
import pakcatt.application.shared.model.AppRequest
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.ProtocolID
import pakcatt.network.radio.kiss.queue.DeliveryQueue
import pakcatt.network.radio.protocol.aprs.model.APRSFrame
import pakcatt.network.radio.protocol.aprs.model.APRSMessageFrame
import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.ResponseType
import pakcatt.network.radio.protocol.aprs.model.APRSDataType
import pakcatt.network.radio.protocol.aprs.model.APRSMicEDataFrame
import pakcatt.network.radio.protocol.shared.ProtocolService
import pakcatt.util.StringUtils

@Service
class APRSService(private val myCall: String,
                  private var appService: AppInterface): ProtocolService() {

    private val logger = LoggerFactory.getLogger(APRSService::class.java)
    private var localDeliveryQueue = ArrayList<KissFrame>()
    private val stringUtils = StringUtils()

    // APRS only uses the Unnumbered INFORMATION frames of AX.25, and must all have a protocol type of NO_LAYER_3
    override fun supportedProtocol(protocolId: Int, controlField: ControlField): Boolean {
        return protocolId == ProtocolID.NO_LAYER_3.id
                && listOf(ControlField.U_UNNUMBERED_INFORMATION,
                    ControlField.U_UNNUMBERED_INFORMATION_P).contains(controlField)
    }

    override fun handleFrame(incomingKissFrame: KissFrame) {
        logger.trace("APRS Service: Handling frame: {}", incomingKissFrame)
        val untypedAPRSFrame = APRSFrame()
        untypedAPRSFrame.populateFromKissFrame(incomingKissFrame)
        logger.trace("Decoded APRS Frame: {}", untypedAPRSFrame.toString())
        val typedAPRSFrame = getTypedAPRSFrame(untypedAPRSFrame)
        logger.debug("Decoded APRS Frame: {}", typedAPRSFrame.toString())
        handleTypedAPRSFrame(typedAPRSFrame)
    }

    override fun queueFramesForDelivery(deliveryQueue: DeliveryQueue) {
        // Queue any adhoc frames requested by apps, for delivery
        for (adhocDelivery in appService.getAdhocResponses(DeliveryType.APRS_FIRE_AND_FORGET)) {
            if (adhocDelivery.deliveryType == DeliveryType.APRS_FIRE_AND_FORGET) {
                queueAPRSMessageForDelivery(adhocDelivery.myCallsign, adhocDelivery.remoteCallsign, adhocDelivery.message)
            }
        }

        // Queue any local frames for delivery
        for (frame in localDeliveryQueue) {
            deliveryQueue.addFrame(frame)
        }
        // Ensure the local delivery queue is empty
        localDeliveryQueue = ArrayList()
    }

    private fun handleTypedAPRSFrame(typedAPRSFrame: APRSFrame) {
        if (typedAPRSFrame.aprsDataType() == APRSDataType.MESSAGE) {
            val aprsMessage = typedAPRSFrame as APRSMessageFrame
            // Make sure we only handle APRS messages addressed to us
            if (aprsMessage.messageDestinationCallsign() == stringUtils.formatCallsignEnsureSSID(myCall)) {
                handleAPRSMessageFrame(aprsMessage)
            }
        }
    }

    private fun handleAPRSMessageFrame(aprsMessage: APRSMessageFrame) {
        // Find an app that will handle this message
        val appRequest = AppRequest(aprsMessage.messageSourceCallsign(), aprsMessage.messageDestinationCallsign(), aprsMessage.message())
        val appResponse = appService.getResponseForReceivedMessage(appRequest)

        // Send an acknowledgement message if a message number was sent to us
        if (aprsMessage.messageNumber() > 0 &&
            (appResponse.responseType == ResponseType.ACK_ONLY ||
            appResponse.responseType == ResponseType.ACK_WITH_TEXT)) {
                val ackMessage = "ack${aprsMessage.messageNumber()}"
            queueAPRSMessageForDelivery(myCall, aprsMessage.messageSourceCallsign(), ackMessage)
        }

        // Send a response message if required
        if (appResponse.responseType == ResponseType.ACK_WITH_TEXT) {
            queueAPRSMessageForDelivery(myCall, aprsMessage.messageSourceCallsign(), appResponse.responseString())
        }
    }

    private fun queueAPRSMessageForDelivery(sourceCallsign: String, destinationCallsign: String, message: String) {
        val aprsMessageFrame = APRSMessageFrame()
        aprsMessageFrame.setSourceCallsign(sourceCallsign)
        aprsMessageFrame.setMessageDestinationCallsign(destinationCallsign)
        aprsMessageFrame.setMessage(message)
        localDeliveryQueue.add(aprsMessageFrame)
    }

    private fun getTypedAPRSFrame(untypedAPRSFrame: APRSFrame): APRSFrame {
        return when (untypedAPRSFrame.aprsDataType()) {
            APRSDataType.MESSAGE -> APRSMessageFrame().populateFromKissFrame(untypedAPRSFrame)
            APRSDataType.MIC_E -> APRSMicEDataFrame().populateFromKissFrame(untypedAPRSFrame)
            APRSDataType.MIC_E_OLD -> APRSMicEDataFrame().populateFromKissFrame(untypedAPRSFrame)
            APRSDataType.MIC_E_DATA -> APRSMicEDataFrame().populateFromKissFrame(untypedAPRSFrame)
            APRSDataType.MIC_E_DATA_OLD -> APRSMicEDataFrame().populateFromKissFrame(untypedAPRSFrame)
            else -> untypedAPRSFrame
        }
    }

}