package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissService

interface NetworkInterface {
    fun queueFrameForDelivery(outgoingFrame: KissFrame)
    fun closeConnection(connectionHandler: ConnectionHandler)
}

@Service
class LinkService(var kissService: KissService,
                  var myCall: String): NetworkInterface {

    private val logger = LoggerFactory.getLogger(LinkService::class.java)
    private var connectionHandlers = HashMap<String, ConnectionHandler>()

    init {
        kissService.setReceiveFrameCallback {
            handleReceivedFrame(it)
        }
    }

    private fun handleReceivedFrame(incomingFrame: KissFrame) {
        if (isMyFrame(incomingFrame)) {
            logger.debug("Frame addressed to me: ${incomingFrame.toString()}")
            val connectionHandler = connectionHandlerForConversation(incomingFrame.sourceCallsign(), incomingFrame.destCallsign())
            connectionHandler.handleIncomingFrame(incomingFrame)
        } else {
            logger.debug("Frame not addressed to me: ${incomingFrame.toString()}")
        }
    }

    private fun isMyFrame(frame: KissFrame): Boolean {
        return frame.destCallsign().equals(myCall, ignoreCase = true)
    }

    private fun connectionHandlerForConversation(fromCallsign: String, toCallsign: String): ConnectionHandler {
        val key = connectionHandlerKey(fromCallsign, toCallsign)
        val connectionHandler = connectionHandlers[key]
        return if (null != connectionHandler) {
            connectionHandler
        } else {
            val connectionHandler = ConnectionHandler(fromCallsign, toCallsign, this)
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
        removeConnectionHandler(connectionHandler.fromCallsign, connectionHandler.toCallsign)
    }

}