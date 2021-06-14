package pakcatt.network.packet.protocol.shared

import pakcatt.network.packet.kiss.model.ControlField
import pakcatt.network.packet.kiss.model.KissFrame
import pakcatt.network.packet.kiss.queue.DeliveryQueue

abstract class ProtocolService {

    abstract fun supportedProtocol(protocolId: Int, controlField: ControlField): Boolean

    abstract fun handleFrame(incomingFrame: KissFrame)

    abstract fun queueFramesForDelivery(deliveryQueue: DeliveryQueue)

}