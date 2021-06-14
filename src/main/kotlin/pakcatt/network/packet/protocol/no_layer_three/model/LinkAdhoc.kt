package pakcatt.network.packet.protocol.no_layer_three.model

enum class DeliveryType {
    REQUIRES_ACK, FIRE_AND_FORGET
}

class LinkAdhoc(val remoteCallsign: String,
                val myCallsign: String,
                val message: String,
                val deliveryType: DeliveryType) {
}