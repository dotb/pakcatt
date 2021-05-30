package pakcatt.application.shared


abstract class PacCattApp {

    protected fun messageNotForMe(destinationCallsign: String, myCallsign: String): Boolean {
        return !destinationCallsign.equals(myCallsign, ignoreCase = true)
    }

    abstract fun handleReceivedMessage(remoteCallSign: String, destinationCallSign: String, receivedMessage: String): AppResponse

}