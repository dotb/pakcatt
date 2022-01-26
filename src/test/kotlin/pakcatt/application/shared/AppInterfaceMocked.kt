package pakcatt.application.shared

import pakcatt.application.shared.model.AdhocMessage
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.DeliveryType

class AppInterfaceMocked: AppInterface {

    var receivedAppRequest: AppRequest? = null

    override fun getDecisionOnConnectionRequest(request: AppRequest): AppResponse {
        return AppResponse.acknowledgeOnly()
    }

    /**
     * Send the AppRequest object back as a response so that it can be asserted.
     * Note, APRS messages are truncated, so interrogating the public receivedAppRequest
     * val is a more appropriate way of testing the accuracy of an AppRequest object.
     */
    override fun getResponseForReceivedMessage(request: AppRequest): AppResponse {
        receivedAppRequest = request
        return AppResponse.sendText(request.toString())
    }

    override fun getAdhocResponses(forDeliveryType: DeliveryType): List<AdhocMessage> {
        TODO("Not yet implemented")
    }

    override fun closeConnection(remoteCallsign: String, myCallsign: String) {
        TODO("Not yet implemented")
    }
}