package pakcatt.network.radio.protocol.packet

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.AppInterface
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.queue.DeliveryQueue
import pakcatt.network.radio.protocol.packet.connection.ConnectionHandler
import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.network.radio.protocol.shared.ProtocolService
import pakcatt.util.SimpleTimer
import java.util.*
import kotlin.collections.HashMap

interface LinkInterface {
    fun getDecisionOnConnectionRequest(request: AppRequest): AppResponse
    fun getResponseForReceivedMessage(request: AppRequest): AppResponse
}

@Service
class PacketService(private var appService: AppInterface,
                    private val myCall: String,
                    private val frameSizeMax: Int,
                    private val framesPerOver: Int,
                    private val minTXPauseSeconds: Int,
                    private val maxDeliveryAttempts: Int,
                    private val deliveryRetryTimeSeconds: Int): LinkInterface, ProtocolService() {

    private val logger = LoggerFactory.getLogger(PacketService::class.java)
    private var connectionHandlers = HashMap<String, ConnectionHandler>()
    private var receiveQueue = LinkedList<KissFrame>()
    private val minPauseTimer = SimpleTimer(minTXPauseSeconds)

    override fun supportedProtocol(protocolId: Int, controlField: ControlField): Boolean {
        // All frame types are handled.
        return true
    }

    override fun handleFrame(incomingFrame: KissFrame) {
        if (myCall.equals(incomingFrame.destCallsign())) {
            logger.trace("Packet Service: handling frame: {}", incomingFrame)
            receiveQueue.add(incomingFrame)
        } else {
            logger.trace("Packet Service: ignoring frame not for me: {}", incomingFrame)
        }
    }

    /**
     * Service the transmit queue of each active connection handler.
     *
     * All unnumbered frames can be removed from the queues, they are fire-and-forget.
     * Numbered frames with a send sequence number, however, must remain in the queue
     * till they are acknowledged or timed out.
     */
    override fun queueFramesForDelivery(deliveryQueue: DeliveryQueue) {
        // First handle received frames
        while (receiveQueue.isNotEmpty()) {
            val nextReceivedFrame = receiveQueue.pop()
            val connectionHandler =
                connectionHandlerForConversation(nextReceivedFrame.channelIdentifier, nextReceivedFrame.sourceCallsign())
                connectionHandler.handleIncomingFrame(nextReceivedFrame)
        }

        // Queue any adhoc frames requested by apps, for delivery
        for (adhocDelivery in appService.getAdhocResponses(DeliveryType.LINK_REQUIRES_ACK)) {
            val adhocConnectionHandler = connectionHandlerForConversation(adhocDelivery.channelIdentifier, adhocDelivery.remoteCallsign)
            adhocConnectionHandler.queueMessageForDelivery(ControlField.INFORMATION_8, adhocDelivery.message)
        }
        for (adhocDelivery in appService.getAdhocResponses(DeliveryType.LINK_FIRE_AND_FORGET)) {
            val adhocConnectionHandler = connectionHandlerForConversation(adhocDelivery.channelIdentifier, adhocDelivery.remoteCallsign)
            adhocConnectionHandler.queueMessageForDelivery(ControlField.U_UNNUMBERED_INFORMATION, adhocDelivery.message)
        }

        // Handle frames queued for delivery
        if (minPauseTimer.hasExpired()) {
            for (connectionHandler in connectionHandlers.values) {
                connectionHandler.queueFramesForDelivery(deliveryQueue)
            }
            if (deliveryQueue.queueSize() > 0) {
                logger.trace("Queued {} frames for delivery", deliveryQueue.queueSize())
                minPauseTimer.reset()
            }
        }
    }

    private fun connectionHandlerForConversation(channelIdentifier: String, remoteCallsign: String): ConnectionHandler {
        val key = connectionHandlerKey(channelIdentifier, remoteCallsign)
        val existingConnectionHandler = connectionHandlers[key]
        return if (null != existingConnectionHandler) {
            existingConnectionHandler
        } else {
            val newConnectionHandler = ConnectionHandler(channelIdentifier, remoteCallsign, myCall, this, frameSizeMax, framesPerOver, maxDeliveryAttempts, deliveryRetryTimeSeconds)
            connectionHandlers[key] = newConnectionHandler
            newConnectionHandler
        }
    }

    private fun connectionHandlerKey(channelIdentifier: String, fromCallsign: String): String {
        return "$channelIdentifier $fromCallsign $myCall"
    }

    /* LinkInterface Methods delegated from ConnectionHandlers */
    override fun getDecisionOnConnectionRequest(request: AppRequest): AppResponse {
        return appService.getDecisionOnConnectionRequest(request)
    }

    override fun getResponseForReceivedMessage(request: AppRequest): AppResponse {
        return appService.getResponseForReceivedMessage(request)
    }

}