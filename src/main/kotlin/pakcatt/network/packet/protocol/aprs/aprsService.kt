package pakcatt.network.packet.protocol.aprs

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.network.packet.kiss.model.ControlField
import pakcatt.network.packet.kiss.model.KissFrame
import pakcatt.network.packet.kiss.model.ProtocolID
import pakcatt.network.packet.kiss.queue.DeliveryQueue
import pakcatt.network.packet.protocol.aprs.model.APRSFrame
import pakcatt.network.packet.protocol.shared.ProtocolService

@Service
class aprsService: ProtocolService() {

    private val logger = LoggerFactory.getLogger(aprsService::class.java)

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

    }

}