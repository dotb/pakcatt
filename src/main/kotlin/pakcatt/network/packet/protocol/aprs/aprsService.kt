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

    override fun supportedProtocol(protocolId: Int, controlField: ControlField): Boolean {
        return protocolId == ProtocolID.APRS.id
    }

    override fun handleFrame(incomingFrame: KissFrame) {
        logger.trace("APRS, handling frame: {}", incomingFrame)
    }

    override fun queueFramesForDelivery(deliveryQueue: DeliveryQueue) {

    }

}