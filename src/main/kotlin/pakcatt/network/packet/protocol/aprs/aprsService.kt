package pakcatt.network.packet.protocol.aprs

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.network.packet.kiss.model.ControlField
import pakcatt.network.packet.kiss.model.KissFrame
import pakcatt.network.packet.kiss.model.ProtocolID
import pakcatt.network.packet.kiss.queue.DeliveryQueue
import pakcatt.network.packet.protocol.shared.ProtocolService

@Service
class aprsService: ProtocolService() {

    private val logger = LoggerFactory.getLogger(aprsService::class.java)

    /**
     * APRS seems to be a little muddled out there, and APRS frames include
     * an APRS ID of 96 or a mixture of numbered and unnumbered information frames.
     */
    override fun supportedProtocol(protocolId: Int, controlField: ControlField): Boolean {
        return protocolId == ProtocolID.NO_LAYER_3.id
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