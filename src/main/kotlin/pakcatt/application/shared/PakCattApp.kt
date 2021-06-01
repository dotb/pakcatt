package pakcatt.application.shared

abstract class PakCattApp {

    protected fun isAddressedToMe(request: AppRequest, myCallsign: String): Boolean {
        return request.addressedToCallsign.equals(myCallsign, ignoreCase = true)
    }

    protected fun notAddressedToMe(request: AppRequest, myCallsign: String): Boolean {
        return !isAddressedToMe(request, myCallsign)
    }

    abstract fun decisionOnConnectionRequest(request: AppRequest): ConnectionResponse

    abstract fun handleReceivedMessage(request: AppRequest): InteractionResponse

}