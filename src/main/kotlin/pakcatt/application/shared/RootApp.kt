package pakcatt.application.shared

import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.LinkResponse

abstract class RootApp: SubApp() {

    protected fun isAddressedToMe(request: LinkRequest, myCallsign: String): Boolean {
        return request.addressedToCallsign.equals(myCallsign, ignoreCase = true)
    }

    protected fun notAddressedToMe(request: LinkRequest, myCallsign: String): Boolean {
        return !isAddressedToMe(request, myCallsign)
    }

    abstract fun decisionOnConnectionRequest(request: LinkRequest): LinkResponse

}