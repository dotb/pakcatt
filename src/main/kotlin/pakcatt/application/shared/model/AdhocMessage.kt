package pakcatt.application.shared.model

enum class DeliveryType {
    LINK_REQUIRES_ACK, LINK_FIRE_AND_FORGET, APRS_FIRE_AND_FORGET
}

class AdhocMessage(val remoteCallsign: String,
                   val myCallsign: String,
                   val message: String,
                   val deliveryType: DeliveryType) {
}