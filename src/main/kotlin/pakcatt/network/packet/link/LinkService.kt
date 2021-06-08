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
import java.util.*
import kotlin.collections.HashMap

interface LinkInterface {
    fun getDecisionOnConnectionRequest(request: LinkRequest): ConnectionResponse
    fun getResponseForReceivedMessage(request: LinkRequest): InteractionResponse
}

@Service
class LinkService(var kissService: KissService,
                  var appService: AppInterface): LinkInterface {

    private val logger = LoggerFactory.getLogger(LinkService::class.java)
    private var connectionHandlers = HashMap<String, ConnectionHandler>()
    private var receiveQueue = LinkedList<KissFrame>()
    private var minOvertimeMilliseconds = 2000
    private var lastTransmitTimestamp: Long = 0

    init {
        kissService.setReceiveFrameCallback {
            handleReceivedFrame(it)
        }
    }
    /**
     * Service the transmit queue of each active connection handler.
     *
     * All unnumbered frames can be removed from the queues, they are fire-and-forget.
     * Numbered frames with a send sequence number, however, must remain in the queue
     * till they are acknowledged or timed out.
     */
    @Scheduled(fixedDelay = 500)
    private fun serviceIOQueues() {

        // First handle received frames
        while (receiveQueue.isNotEmpty()) {
            val nextReceivedFrame = receiveQueue.pop()
            val connectionHandler =
                connectionHandlerForConversation(nextReceivedFrame.sourceCallsign(), nextReceivedFrame.destCallsign())
                connectionHandler.handleIncomingFrame(nextReceivedFrame)
        }

        // Handle frames queued for delivery
        if (Date().time - minOvertimeMilliseconds > lastTransmitTimestamp) {
            var deliveryCount = 0
            for (connectionHandler in connectionHandlers.values) {
                deliveryCount = connectionHandler.deliverQueuedControlFrame(kissService)
                deliveryCount += connectionHandler.deliverSequencedFrames(kissService)
            }
            if (deliveryCount > 0) {
                logger.debug("Transmitted: {} frames", deliveryCount)
                lastTransmitTimestamp = Date().time
            }
        }
    }

    private fun handleReceivedFrame(incomingFrame: KissFrame) {
        receiveQueue.add(incomingFrame)
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

    fun closeConnection(remoteCallsign: String, myCallsign: String) {
        appService.closeConnection(remoteCallsign, myCallsign)
        connectionHandlers.remove(connectionHandlerKey(remoteCallsign, myCallsign))
        logger.debug("Removed connection handler for {}", remoteCallsign)
    }

    /* LinkInterface Methods delegated from ConnectionHandlers */
    override fun getDecisionOnConnectionRequest(request: LinkRequest): ConnectionResponse {
        return appService.getDecisionOnConnectionRequest(request)
    }

    override fun getResponseForReceivedMessage(request: LinkRequest): InteractionResponse {
        return appService.getResponseForReceivedMessage(request)
    }

}