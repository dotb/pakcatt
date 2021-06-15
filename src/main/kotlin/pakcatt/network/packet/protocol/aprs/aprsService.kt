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
        return protocolId == ProtocolID.APRS.id || listOf(ControlField.INFORMATION_8,
            ControlField.INFORMATION_8_P, ControlField.U_UNNUMBERED_INFORMATION,
            ControlField.U_UNNUMBERED_INFORMATION_P, ControlField.INFORMATION_128,
            ControlField.INFORMATION_128_P).contains(controlField)
    }

    override fun handleFrame(incomingFrame: KissFrame) {
        logger.trace("APRS, handling frame: {}", incomingFrame)
    }

    override fun queueFramesForDelivery(deliveryQueue: DeliveryQueue) {

    }

}