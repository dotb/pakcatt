package pakcatt.application.shared

import pakcatt.network.packet.link.model.DeliveryType
import pakcatt.network.packet.link.model.LinkAdhoc
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.LinkResponse

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