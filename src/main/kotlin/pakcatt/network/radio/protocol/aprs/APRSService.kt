package pakcatt.network.radio.protocol.aprs

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.AppInterface
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.ProtocolID
import pakcatt.network.radio.kiss.queue.DeliveryQueue
import pakcatt.application.shared.model.DeliveryType
import pakcatt.network.radio.protocol.aprs.handlers.APRSMessageHandler
import pakcatt.network.radio.protocol.aprs.handlers.APRSMicEDataHandler
import pakcatt.network.radio.protocol.aprs.model.*
import pakcatt.network.radio.protocol.shared.ProtocolService
import pakcatt.util.StringUtils

@Service
class APRSService(private val myCall: String,
                  private val appInterface: AppInterface,
                  private val aprsQueue: APRSQueue): ProtocolService() {

    private val logger = LoggerFactory.getLogger(APRSService::class.java)
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
        logger.trace("Untyped APRS Frame: {}", untypedAPRSFrame.toString())
        val typedAPRSFrame = getTypedAPRSFrame(untypedAPRSFrame)
        logger.debug("Typed APRS Frame: {}", typedAPRSFrame.toString())
        handleTypedAPRSFrame(typedAPRSFrame)
    }

    override fun queueFramesForDelivery(deliveryQueue: DeliveryQueue) {
        // Queue any adhoc frames requested by apps, for delivery
        for (adhocDelivery in appInterface.getAdhocResponses(DeliveryType.APRS_FIRE_AND_FORGET)) {
            if (adhocDelivery.deliveryType == DeliveryType.APRS_FIRE_AND_FORGET) {
                aprsQueue.queueAPRSMessageForDelivery(adhocDelivery.myCallsign, adhocDelivery.remoteCallsign, adhocDelivery.message)
            }
        }

        // Queue any local frames for delivery
        val framesForDelivery = aprsQueue.flushQueue()
        for (frame in framesForDelivery) {
            deliveryQueue.addFrame(frame)
        }
    }

    private fun handleTypedAPRSFrame(typedAPRSFrame: APRSFrame) {
        when (typedAPRSFrame.aprsDataType()) {
            APRSDataType.MESSAGE -> APRSMessageHandler(myCall, aprsQueue, appInterface, stringUtils).handleAPRSFrame(typedAPRSFrame)
            APRSDataType.MIC_E -> APRSMicEDataHandler(myCall, aprsQueue, appInterface, stringUtils).handleAPRSFrame(typedAPRSFrame)
            APRSDataType.MIC_E_OLD -> APRSMicEDataHandler(myCall, aprsQueue, appInterface, stringUtils).handleAPRSFrame(typedAPRSFrame)
            APRSDataType.MIC_E_DATA -> APRSMicEDataHandler(myCall, aprsQueue, appInterface, stringUtils).handleAPRSFrame(typedAPRSFrame)
            APRSDataType.MIC_E_DATA_OLD -> APRSMicEDataHandler(myCall, aprsQueue, appInterface, stringUtils).handleAPRSFrame(typedAPRSFrame)
            else -> {
                logger.error("PakCatt does not yet support frame type: {}", typedAPRSFrame)
                logger.error("APRS frame data: {}", stringUtils.byteArrayToHex(typedAPRSFrame.packetData()))
            }
        }
    }

    private fun getTypedAPRSFrame(untypedAPRSFrame: APRSFrame): APRSFrame {
        return when (untypedAPRSFrame.aprsDataType()) {
            APRSDataType.MESSAGE -> APRSMessageFrame().populateFromKissFrame(untypedAPRSFrame)
            APRSDataType.MIC_E -> APRSMicEDataFrame().populateFromKissFrame(untypedAPRSFrame)
            APRSDataType.MIC_E_OLD -> APRSMicEDataFrame().populateFromKissFrame(untypedAPRSFrame)
            APRSDataType.MIC_E_DATA -> APRSMicEDataFrame().populateFromKissFrame(untypedAPRSFrame)
            APRSDataType.MIC_E_DATA_OLD -> APRSMicEDataFrame().populateFromKissFrame(untypedAPRSFrame)
            else -> {
                logger.error("PakCatt does not yet handle frame: {}", untypedAPRSFrame)
                logger.error("APRS frame data: {}", stringUtils.byteArrayToHex(untypedAPRSFrame.packetData()))
                untypedAPRSFrame
            }
        }
    }

}