package pakcatt.application.shared.model

enum class DeliveryType {
    LINK_REQUIRES_ACK, LINK_FIRE_AND_FORGET, APRS_FIRE_AND_FORGET
}

class AdhocMessage(val channelIdentifier: String,
                   val remoteCallsign: String,
                   val myCallsign: String,
                   val message: String,
                   val deliveryType: DeliveryType) {

    override fun toString(): String {
        return "Chan: $channelIdentifier From: $myCallsign To: $remoteCallsign Message: $message"
    }
}