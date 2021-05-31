package pakcatt.application.shared

import org.springframework.beans.factory.annotation.Autowired
import pakcatt.network.packet.link.LinkService


abstract class PacCattApp {

    @Autowired
    private lateinit var linkService: LinkService

    protected fun isAddressedToMe(request: AppRequest, myCallsign: String): Boolean {
        return request.addressedToCallsign.equals(myCallsign, ignoreCase = true)
    }

    protected fun notAddressedToMe(request: AppRequest, myCallsign: String): Boolean {
        return !isAddressedToMe(request, myCallsign)
    }

    protected fun sendMessage(remoteCallsign: String, myCallsign: String, message: String) {
        val messageRequest = AppRequest(remoteCallsign, myCallsign, message)
        linkService.handleRequestToSendMessageFromApp(messageRequest)
    }

    abstract fun decisionOnConnectionRequest(request: AppRequest): ConnectionResponse

    abstract fun handleReceivedMessage(request: AppRequest): InteractionResponse

}