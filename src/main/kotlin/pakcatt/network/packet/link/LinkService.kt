package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.PacCattApp
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissService

interface NetworkInterface {
    fun queueFrameForDelivery(outgoingFrame: KissFrame)
    fun closeConnection(connectionHandler: ConnectionHandler)
}

@Service
class LinkService(var kissService: KissService,
                  val applications: List<PacCattApp>): NetworkInterface {

    private val logger = LoggerFactory.getLogger(LinkService::class.java)
    private var connectionHandlers = HashMap<String, ConnectionHandler>()

    init {
        kissService.setReceiveFrameCallback {
            handleReceivedFrame(it)
        }
    }

    private fun handleReceivedFrame(incomingFrame: KissFrame) {
        val connectionHandler = connectionHandlerForConversation(incomingFrame.sourceCallsign(), incomingFrame.destCallsign())
        connectionHandler.handleIncomingFrame(incomingFrame)
    }

    private fun connectionHandlerForConversation(fromCallsign: String, toCallsign: String): ConnectionHandler {
        val key = connectionHandlerKey(fromCallsign, toCallsign)
        val connectionHandler = connectionHandlers[key]
        return if (null != connectionHandler) {
            connectionHandler
        } else {
            val connectionHandler = ConnectionHandler(fromCallsign, toCallsign, this, applications)
            connectionHandlers[key] = connectionHandler
            connectionHandler
        }
    }

    private fun removeConnectionHandler(fromCallsign: String, toCallsign: String) {
        connectionHandlers.remove(connectionHandlerKey(fromCallsign, toCallsign))
    }

    private fun connectionHandlerKey(fromCallsign: String, toCallsign: String): String {
        return "$fromCallsign $toCallsign"
    }

    /* Network Interface methods */
    override fun queueFrameForDelivery(outgoingFrame: KissFrame) {
        kissService.queueFrameForTransmission(outgoingFrame)
    }

    override fun closeConnection(connectionHandler: ConnectionHandler) {
        removeConnectionHandler(connectionHandler.remoteCallsign, connectionHandler.myCallsign)
    }

}