package pakcatt.network.radio.protocol.shared

import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.queue.DeliveryQueue

abstract class ProtocolService {

    abstract fun supportedProtocol(protocolId: Int, controlField: ControlField): Boolean

    abstract fun handleFrame(incomingFrame: KissFrame)

    abstract fun queueFramesForDelivery(deliveryQueue: DeliveryQueue)

}