package pakcatt.application.shared

import org.slf4j.LoggerFactory
import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.AdhocMessage
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.network.radio.kiss.KissService

abstract class RootApp: SubApp() {

    private val logger = LoggerFactory.getLogger(RootApp::class.java)
    private var adhocResponses = ArrayList<AdhocMessage>()

    protected fun isAddressedToMe(request: AppRequest, myCallsign: String): Boolean {
        return request.addressedToCallsign.equals(myCallsign, ignoreCase = true)
    }

    protected fun notAddressedToMe(request: AppRequest, myCallsign: String): Boolean {
        return !isAddressedToMe(request, myCallsign)
    }

    abstract fun decisionOnConnectionRequest(request: AppRequest): AppResponse

    override fun queueAdhocMessageForTransmission(
            transmissionChannelIdentifier: String,
            remoteCallsign: String,
            myCallsign: String,
            message: String,
            deliveryType: DeliveryType) {

        val adhocMessage = AdhocMessage(transmissionChannelIdentifier, remoteCallsign, myCallsign, message, deliveryType)
        adhocResponses.add(adhocMessage)
        logger.trace("Added adhoc message to queue for delivery {}", adhocMessage)
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