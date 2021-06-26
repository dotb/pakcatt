package pakcatt.application.shared

import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.AdhocMessage
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse

abstract class RootApp: SubApp() {

    private var adhocResponses = ArrayList<AdhocMessage>()

    protected fun isAddressedToMe(request: AppRequest, myCallsign: String): Boolean {
        return request.addressedToCallsign.equals(myCallsign, ignoreCase = true)
    }

    protected fun notAddressedToMe(request: AppRequest, myCallsign: String): Boolean {
        return !isAddressedToMe(request, myCallsign)
    }

    abstract fun decisionOnConnectionRequest(request: AppRequest): AppResponse

    override fun queueAdhocMessageForTransmission(remoteCallsign: String,
                                         myCallsign: String,
                                         message: String,
                                         deliveryType: DeliveryType) {
        adhocResponses.add(AdhocMessage(remoteCallsign, myCallsign, message, deliveryType))
    }

    fun flushAdhocResponses(forDeliveryType: DeliveryType): List<AdhocMessage> {
        val deliveryList = ArrayList<AdhocMessage>()
        for (adhocResponse in adhocResponses) {
            if (forDeliveryType == adhocResponse.deliveryType) {
                deliveryList.add(adhocResponse)
            }
        }
        adhocResponses.removeAll(deliveryList)
        return deliveryList
    }

}