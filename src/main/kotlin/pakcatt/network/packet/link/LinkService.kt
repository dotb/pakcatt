package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pakcatt.application.shared.AppInterface
import pakcatt.network.packet.link.model.ConnectionResponse
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissService
import pakcatt.network.packet.link.model.LinkRequest

interface LinkInterface {
    fun getDecisionOnConnectionRequest(request: LinkRequest): ConnectionResponse
    fun getResponseForReceivedMessage(request: LinkRequest): InteractionResponse
    fun closeConnection(remoteCallsign: String, myCallsign: String)
}

@Service
class LinkService(var kissService: KissService,
                  var appService: AppInterface): LinkInterface {

    private val logger = LoggerFactory.getLogger(LinkService::class.java)
    private var connectionHandlers = HashMap<String, ConnectionHandler>()

    init {
        kissService.setReceiveFrameCallback {
            handleReceivedFrame(it)
        }
    }

    @Scheduled(fixedRate = 2000)
    private fun serviceTransmitQueues() {
        for (connectionHandler in connectionHandlers.values) {
            val nextFrame = connectionHandler.transmitQueue.peekFirst()
            kissService.transmitFrame(nextFrame)
        }
    }

    private fun handleReceivedFrame(incomingFrame: KissFrame) {
        val connectionHandler = connectionHandlerForConversation(incomingFrame.sourceCallsign(), incomingFrame.destCallsign())
        connectionHandler.handleIncomingFrame(incomingFrame)
    }

    private fun connectionHandlerForConversation(remoteCallsign: String, addressedToCallsign: String): ConnectionHandler {
        val key = connectionHandlerKey(remoteCallsign, addressedToCallsign)
        val connectionHandler = connectionHandlers[key]
        return if (null != connectionHandler) {
            connectionHandler
        } else {
            val connectionHandler = ConnectionHandler(remoteCallsign, addressedToCallsign, this)
            connectionHandlers[key] = connectionHandler
            connectionHandler
        }
    }

    private fun connectionHandlerKey(fromCallsign: String, toCallsign: String): String {
        return "$fromCallsign $toCallsign"
    }

    /* LinkInterface Methods delegated from ConnectionHandlers */
    override fun getDecisionOnConnectionRequest(request: LinkRequest): ConnectionResponse {
        return appService.getDecisionOnConnectionRequest(request)
    }

    override fun getResponseForReceivedMessage(request: LinkRequest): InteractionResponse {
        return appService.getResponseForReceivedMessage(request)
    }

    override fun closeConnection(remoteCallsign: String, myCallsign: String) {
        appService.closeConnection(remoteCallsign, myCallsign)
        connectionHandlers.remove(connectionHandlerKey(remoteCallsign, myCallsign))
        logger.debug("Disconnected from {}", remoteCallsign)
    }

}