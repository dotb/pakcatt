package pakcatt.network.packet.protocol.no_layer_three

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.AppInterface
import pakcatt.network.packet.kiss.model.ControlField
import pakcatt.network.packet.kiss.model.KissFrame
import pakcatt.network.packet.kiss.model.ProtocolID
import pakcatt.network.packet.kiss.queue.DeliveryQueue
import pakcatt.network.packet.protocol.no_layer_three.connection.ConnectionHandler
import pakcatt.network.packet.protocol.no_layer_three.model.DeliveryType
import pakcatt.network.packet.protocol.no_layer_three.model.LinkRequest
import pakcatt.network.packet.protocol.no_layer_three.model.LinkResponse
import pakcatt.network.packet.protocol.shared.ProtocolService
import java.util.*
import kotlin.collections.HashMap

interface LinkInterface {
    fun getDecisionOnConnectionRequest(request: LinkRequest): LinkResponse
    fun getResponseForReceivedMessage(request: LinkRequest): LinkResponse
}

@Service
class LinkService(private var appService: AppInterface,
                  private val frameSizeMax: Int,
                  private val framesPerOver: Int,
                  private val minTXPauseSeconds: Int,
                  private val maxDeliveryAttempts: Int,
                  private val deliveryRetryTimeSeconds: Int): LinkInterface, ProtocolService() {

    private val logger = LoggerFactory.getLogger(LinkService::class.java)
    private var connectionHandlers = HashMap<String, ConnectionHandler>()
    private var receiveQueue = LinkedList<KissFrame>()
    private var lastTransmitTimestamp: Long = 0

    override fun supportedProtocol(protocolId: Int, controlField: ControlField): Boolean {
        return protocolId == ProtocolID.NO_LAYER_3.id || listOf(ControlField.S_8_RECEIVE_READY,
            ControlField.S_8_RECEIVE_NOT_READY, ControlField.S_8_REJECT, ControlField.S_8_SELECTIVE_REJECT,
            ControlField.S_8_RECEIVE_READY_P, ControlField.S_8_RECEIVE_NOT_READY_P, ControlField.S_8_REJECT_P,
            ControlField.S_8_SELECTIVE_REJECT_P, ControlField.S_128_RECEIVE_READY, ControlField.S_128_RECEIVE_NOT_READY,
            ControlField.S_128_REJECT, ControlField.S_128_SELECTIVE_REJECT, ControlField.S_128_RECEIVE_READY_P,
            ControlField.S_128_RECEIVE_NOT_READY_P, ControlField.S_128_REJECT_P, ControlField.S_128_SELECTIVE_REJECT_P,
            ControlField.U_SET_ASYNC_BALANCED_MODE_EXTENDED, ControlField.U_SET_ASYNC_BALANCED_MODE,
            ControlField.U_DISCONNECT, ControlField.U_DISCONNECT_MODE, ControlField.U_UNNUMBERED_ACKNOWLEDGE,
            ControlField.U_REJECT, ControlField.U_UNNUMBERED_INFORMATION, ControlField.U_EXCHANGE_IDENTIFICATION,
            ControlField.U_TEST, ControlField.U_SET_ASYNC_BALANCED_MODE_EXTENDED_P, ControlField.U_SET_ASYNC_BALANCED_MODE_P,
            ControlField.U_DISCONNECT_P, ControlField.U_DISCONNECT_MODE_P, ControlField.U_UNNUMBERED_ACKNOWLEDGE_P,
            ControlField.U_REJECT_P, ControlField.U_UNNUMBERED_INFORMATION_P, ControlField.U_EXCHANGE_IDENTIFICATION_P,
            ControlField.U_TEST_P).contains(controlField)
    }

    override fun handleFrame(incomingFrame: KissFrame) {
        logger.trace("No Layer 3, handling frame: {}", incomingFrame)
        receiveQueue.add(incomingFrame)
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
                connectionHandlerForConversation(nextReceivedFrame.sourceCallsign(), nextReceivedFrame.destCallsign())
                connectionHandler.handleIncomingFrame(nextReceivedFrame)
        }

        // Queue any adhoc frames requested by apps, for delivery
        for (adhocDelivery in appService.getAdhocResponsesForDelivery()) {
            val adhocConnectionHandler = connectionHandlerForConversation(adhocDelivery.remoteCallsign, adhocDelivery.myCallsign)
            when (adhocDelivery.deliveryType) {
                DeliveryType.REQUIRES_ACK -> adhocConnectionHandler.queueMessageForDelivery(ControlField.INFORMATION_8, adhocDelivery.message)
                DeliveryType.FIRE_AND_FORGET -> adhocConnectionHandler.queueMessageForDelivery(ControlField.U_UNNUMBERED_INFORMATION, adhocDelivery.message)
            }
        }

        // Handle frames queued for delivery
        val minTXPauseMilliseconds = minTXPauseSeconds * 1000
        if (Date().time - minTXPauseMilliseconds > lastTransmitTimestamp) {
            var deliveryCount = 0
            for (connectionHandler in connectionHandlers.values) {
                deliveryCount += connectionHandler.deliverQueuedControlFrame(deliveryQueue)
                deliveryCount += connectionHandler.deliverContentFrames(deliveryQueue)
            }
            if (deliveryCount > 0) {
                logger.debug("Transmitting: {} frames", deliveryCount)
                lastTransmitTimestamp = Date().time
            }
        }
    }

    private fun connectionHandlerForConversation(remoteCallsign: String, myCallsign: String): ConnectionHandler {
        val key = connectionHandlerKey(remoteCallsign, myCallsign)
        val connectionHandler = connectionHandlers[key]
        return if (null != connectionHandler) {
            connectionHandler
        } else {
            val connectionHandler = ConnectionHandler(remoteCallsign, myCallsign, this, frameSizeMax, framesPerOver, maxDeliveryAttempts, deliveryRetryTimeSeconds)
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
    override fun getDecisionOnConnectionRequest(request: LinkRequest): LinkResponse {
        return appService.getDecisionOnConnectionRequest(request)
    }

    override fun getResponseForReceivedMessage(request: LinkRequest): LinkResponse {
        return appService.getResponseForReceivedMessage(request)
    }

}