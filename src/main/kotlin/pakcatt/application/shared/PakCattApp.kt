package pakcatt.application.shared

import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.ConnectionResponse
import pakcatt.network.packet.link.model.InteractionResponse

abstract class PakCattApp {

    protected fun isAddressedToMe(request: LinkRequest, myCallsign: String): Boolean {
        return request.addressedToCallsign.equals(myCallsign, ignoreCase = true)
    }

    protected fun notAddressedToMe(request: LinkRequest, myCallsign: String): Boolean {
        return !isAddressedToMe(request, myCallsign)
    }

    abstract fun decisionOnConnectionRequest(request: LinkRequest): ConnectionResponse

    abstract fun handleReceivedMessage(request: LinkRequest): InteractionResponse

}