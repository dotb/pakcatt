package pakcatt.application.shared

import pakcatt.network.radio.protocol.packet.model.DeliveryType
import pakcatt.network.radio.protocol.packet.model.LinkAdhoc
import pakcatt.network.radio.protocol.packet.model.LinkRequest
import pakcatt.network.radio.protocol.packet.model.LinkResponse

abstract class RootApp: SubApp() {

    private var adhocResponses = ArrayList<LinkAdhoc>()

    protected fun isAddressedToMe(request: LinkRequest, myCallsign: String): Boolean {
        return request.addressedToCallsign.equals(myCallsign, ignoreCase = true)
    }

    protected fun notAddressedToMe(request: LinkRequest, myCallsign: String): Boolean {
        return !isAddressedToMe(request, myCallsign)
    }

    abstract fun decisionOnConnectionRequest(request: LinkRequest): LinkResponse

    override fun queueAdhocMessageForTransmission(remoteCallsign: String,
                                         myCallsign: String,
                                         message: String,
                                         deliveryType: DeliveryType) {
        adhocResponses.add(LinkAdhoc(remoteCallsign, myCallsign, message, deliveryType))
    }

    fun flushAdhocResponses(forDeliveryType: DeliveryType): List<LinkAdhoc> {
        val deliveryList = ArrayList<LinkAdhoc>()
        for (adhocResponse in adhocResponses) {
            if (forDeliveryType == adhocResponse.deliveryType) {
                deliveryList.add(adhocResponse)
            }
        }
        adhocResponses.removeAll(deliveryList)
        return deliveryList
    }

}