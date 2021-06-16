package pakcatt.network.radio.protocol.aprs

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.AppInterface
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.ProtocolID
import pakcatt.network.radio.kiss.queue.DeliveryQueue
import pakcatt.network.radio.protocol.aprs.model.APRSFrame
import pakcatt.network.radio.protocol.aprs.model.APRSMessageFrame
import pakcatt.network.radio.protocol.packet.model.DeliveryType
import pakcatt.network.radio.protocol.shared.ProtocolService

@Service
class APRSService(private var appService: AppInterface): ProtocolService() {

    private val logger = LoggerFactory.getLogger(APRSService::class.java)

    // APRS only uses the Unnumbered INFORMATION frames of AX.25, and must all have a protocol type of NO_LAYER_3
    override fun supportedProtocol(protocolId: Int, controlField: ControlField): Boolean {
        return protocolId == ProtocolID.NO_LAYER_3.id
                && listOf(ControlField.U_UNNUMBERED_INFORMATION,
                    ControlField.U_UNNUMBERED_INFORMATION_P).contains(controlField)
    }

    override fun handleFrame(incomingKissFrame: KissFrame) {
        logger.trace("APRS: Handling frame: {}", incomingKissFrame)
        val aprsFrame = APRSFrame()
        aprsFrame.populateFromKissFrame(incomingKissFrame)
        logger.debug("Decoded APRS Frame: {}", aprsFrame.toString())
    }

    override fun queueFramesForDelivery(deliveryQueue: DeliveryQueue) {
        // Queue any adhoc frames requested by apps, for delivery
        for (adhocDelivery in appService.getAdhocResponses(DeliveryType.APRS_FIRE_AND_FORGET)) {
            if (adhocDelivery.deliveryType == DeliveryType.APRS_FIRE_AND_FORGET) {
                val aprsMessageFrame = APRSMessageFrame()
                aprsMessageFrame.setSourceCallsign(adhocDelivery.myCallsign)
                aprsMessageFrame.setMessageDestinationCallsign(adhocDelivery.remoteCallsign)
                aprsMessageFrame.setMessage(adhocDelivery.message)
                deliveryQueue.addFrame(aprsMessageFrame)
            }
        }
    }

}