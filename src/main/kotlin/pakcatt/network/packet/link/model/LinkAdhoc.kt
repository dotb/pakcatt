package pakcatt.network.packet.link.model

enum class DeliveryType {
    REQUIRES_ACK, FIRE_AND_FORGET
}

class LinkAdhoc(val remoteCallsign: String,
                val myCallsign: String,
                val message: String,
                val deliveryType: DeliveryType) {
}