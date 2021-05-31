package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.AppRequest
import pakcatt.application.shared.ConnectionResponse
import pakcatt.application.shared.InteractionResponse
import pakcatt.application.shared.PacCattApp
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissService

interface NetworkInterface {
    fun decisionOnConnectionRequest(request: AppRequest): ConnectionResponse
    fun handleReceivedMessage(request: AppRequest): InteractionResponse
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
    override fun decisionOnConnectionRequest(request: AppRequest): ConnectionResponse {
        var finalConnectionDecision = ConnectionResponse.ignore()
        for (app in applications) {
            val connectionDecision = app.decisionOnConnectionRequest(request)
            when (connectionDecision.responseType) {
                // Return with the commitment to connect with a welcome message
                ConnectionResponse.ConnectionResponseType.CONNECT_WITH_MESSAGE -> return connectionDecision
                // Follow through with the connection, but see if another app has a welcome message
                ConnectionResponse.ConnectionResponseType.CONNECT -> finalConnectionDecision = connectionDecision
                // Do nothing, this app wants to ignore this connection
                ConnectionResponse.ConnectionResponseType.IGNORE -> logger.trace("App isn't interested in this connection.")
            }
        }
        return finalConnectionDecision
    }

    override fun handleReceivedMessage(request: AppRequest): InteractionResponse {
        var finalInteractionResponse = InteractionResponse.ignore()
        for (app in applications) {
            val interactionResponse = app.handleReceivedMessage(request)
            when (interactionResponse.responseType) {
                // If an app wants to ACK and send a message, return right away
                InteractionResponse.InteractionResponseType.SEND_TEXT -> return interactionResponse
                // If an app wants to ACK, continue to search for an app that wants to return a message, too
                InteractionResponse.InteractionResponseType.ACK_ONLY -> finalInteractionResponse = interactionResponse
                // Do nothing, this app doesn't want to handle this request
                InteractionResponse.InteractionResponseType.IGNORE -> logger.trace("App isn't interested in responding.")
            }
        }
        return finalInteractionResponse
    }

    override fun queueFrameForDelivery(outgoingFrame: KissFrame) {
        kissService.queueFrameForTransmission(outgoingFrame)
    }

    override fun closeConnection(connectionHandler: ConnectionHandler) {
        removeConnectionHandler(connectionHandler.remoteCallsign, connectionHandler.myCallsign)
        logger.debug("Disconnected from ${connectionHandler.remoteCallsign}")
    }

}