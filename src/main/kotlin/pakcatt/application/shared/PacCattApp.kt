package pakcatt.application.shared


abstract class PacCattApp {

    abstract fun handleReceivedMessage(receivedMessage: String): AppResponse

}