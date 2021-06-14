package pakcatt.application.shared

import pakcatt.network.packet.protocol.no_layer_three.model.DeliveryType
import pakcatt.network.packet.protocol.no_layer_three.model.LinkAdhoc
import pakcatt.network.packet.protocol.no_layer_three.model.LinkRequest
import pakcatt.network.packet.protocol.no_layer_three.model.LinkResponse

abstract class RootApp: SubApp() {

    private var adhocResponses = ArrayList<LinkAdhoc>()

    protected fun isAddressedToMe(request: LinkRequest, myCallsign: String): Boolean {
        return request.addressedToCallsign.equals(myCallsign, ignoreCase = true)
    }

    protected fun notAddressedToMe(request: LinkRequest, myCallsign: String): Boolean {
        return !isAddressedToMe(request, myCallsign)
    }

    abstract fun decisionOnConnectionRequest(request: LinkRequest): LinkResponse

    fun queueAdhocMessageForTransmission(remoteCallsign: String,
                                         myCallsign: String,
                                         message: String,
                                         deliveryType: DeliveryType) {
        adhocResponses.add(LinkAdhoc(remoteCallsign, myCallsign, message, deliveryType))
    }

    fun flushAdhocResponses(): List<LinkAdhoc> {
        val responsesForDelivery = adhocResponses
        adhocResponses = ArrayList()
        return responsesForDelivery
    }

}