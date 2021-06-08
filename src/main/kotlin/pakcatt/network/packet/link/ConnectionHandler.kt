package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import pakcatt.network.packet.kiss.*
import pakcatt.network.packet.link.model.*
import kotlin.collections.ArrayList

enum class ControlMode {
    MODULO_8, MODULO_128
}

class ConnectionHandler(val remoteCallsign: String,
                        val myCallsign: String,
                        val linkInterface: LinkInterface) {

    private val logger = LoggerFactory.getLogger(ConnectionHandler::class.java)
    private var controlMode = ControlMode.MODULO_8
    private var nextQueuedControlFrame: KissFrame? = null
    private var sequencedQueue = SequencedQueue()

    /* The receive state variable contains the sequence number of the next expected received I frame.
     * This variable is updated upon the reception of an error-free I frame whose send sequence number
     * equals the present received state variable value. */
    private var nextExpectedSendSequenceNumberFromPeer = 0

    fun handleIncomingFrame(incomingFrame: KissFrame) {
        when (incomingFrame.controlFrame()) {
            ControlFrame.U_SET_ASYNC_BALANCED_MODE_P -> handleConnectionRequest(incomingFrame) // Connection request
            ControlFrame.INFORMATION_8 -> handleNumberedInformationFrame(incomingFrame) // Application info frame
            ControlFrame.INFORMATION_8_P -> handleNumberedInformationFrame(incomingFrame) // Application info frame
            ControlFrame.S_8_RECEIVE_READY -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.S_8_RECEIVE_READY_P -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.S_8_REJECT -> handleIncomingAcknowledgement(incomingFrame)
//            ControlFrame.S_8_REJECT_P -> handleRequestForState() // Works better without transferring state on S_8_REJECT_P
            ControlFrame.U_REJECT -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.U_DISCONNECT_P -> handleDisconnectRequest() // Disconnect request
            else -> ignoreFrame(incomingFrame)
        }
    }

    fun deliverQueuedControlFrame(kissService: KissService): Int {
        val controlFrame = nextQueuedControlFrame
        return if (null != controlFrame) {
            kissService.transmitFrame(controlFrame)
            nextQueuedControlFrame = null
            1
        } else {
            0
        }
    }

    fun deliverSequencedFrames(kissService: KissService): Int {
        var deliveryCount = 0
        val framesForDelivery = sequencedQueue.getSequencedFramesForDelivery()
        if (framesForDelivery.isNotEmpty()) {
            for ((index, frame) in framesForDelivery.withIndex()) {

                // This is the last frame to be delivered in this over, set the P flag.
                if (index >= framesForDelivery.size - 1 && !sequencedQueue.isAtEndOfMessageDelivery()) {
                    frame.setControlType(ControlFrame.INFORMATION_8_P)
                }
                kissService.transmitFrame(frame)
                deliveryCount++
            }

            // If we're at the end of a message, transmit READY_RECEIVE_P
            if (sequencedQueue.isAtEndOfMessageDelivery()) {
                kissService.transmitFrame(newResponseFrame(ControlFrame.S_8_RECEIVE_READY_P, false))
                deliveryCount++
            }
        }
        return deliveryCount
    }

    private fun queueFrameForControl(frame: KissFrame) {
        nextQueuedControlFrame = frame
    }

    /* Application interface */
    private fun handleNumberedInformationFrame(incomingFrame: KissFrame) {
        // Check that we expected this frame, and haven't missed any or mixed up the order
        if (incomingFrame.sendSequenceNumber() == nextExpectedSendSequenceNumberFromPeer) {
            incrementReceiveSequenceNumber()
            handleIncomingAcknowledgement(incomingFrame)

            // Share the payload with any listening applications to process
            val  appResponse = linkInterface.getResponseForReceivedMessage(LinkRequest(incomingFrame.sourceCallsign(), incomingFrame.destCallsign(), incomingFrame.payloadDataString()))
            when (appResponse.responseType) {
                InteractionResponseType.SEND_TEXT -> sendMessage(appResponse.responseString())
                InteractionResponseType.ACK_ONLY -> sendAcknowlegeAndReadyForReceive()
                InteractionResponseType.IGNORE -> logger.trace("Apps ignored frame: ${incomingFrame.toString()}")
            }
        } else {
            rejectUnsequencedFrame(incomingFrame)
        }
    }

    /* Link layer handlers */
    private fun handleConnectionRequest(incomingFrame: KissFrame) {
        // Gather a connection decision from applications
        val appResponse = linkInterface.getDecisionOnConnectionRequest(LinkRequest(incomingFrame.sourceCallsign(), incomingFrame.destCallsign(), incomingFrame.payloadDataString()))
        when (appResponse.responseType) {
            ConnectionResponseType.CONNECT -> acceptIncomingConnection()
            ConnectionResponseType.CONNECT_WITH_MESSAGE -> acceptIncomingConnectionWithMessage(appResponse.responseString())
            ConnectionResponseType.IGNORE -> logger.trace("Ignored connection request from: $remoteCallsign to :$myCallsign")
        }
    }

    private fun acceptIncomingConnection() {
        logger.info("Accepting connection from remote party: $remoteCallsign local party: $myCallsign")
        // Reset sequence state
        controlMode = ControlMode.MODULO_8
        sequencedQueue.reset()
        nextQueuedControlFrame = null
        nextExpectedSendSequenceNumberFromPeer = 0
        val frame = newResponseFrame(ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        queueFrameForControl(frame)
    }

    private fun acceptIncomingConnectionWithMessage(message: String) {
        acceptIncomingConnection()
        sendMessage(message)
    }

    private fun handleIncomingAcknowledgement(incomingFrame: KissFrame) {
        // If our record of our last acknowledged sent frame is already updated, then the remote party may be asking us to re-sync with an RR_P
        if (sequencedQueue.handleIncomingAcknowledgementAndIfRepeated(incomingFrame)
            && (incomingFrame.controlFrame() == ControlFrame.S_8_RECEIVE_READY_P
                    || incomingFrame.controlFrame() == ControlFrame.S_128_RECEIVE_READY_P)) {
            logger.debug("Received multiple of the same acknowledgement sequence number. Sending an Ready_Receive_P to re-sync.")
            handleRequestForState()
        }
    }

    private fun sendAcknowlegeAndReadyForReceive() {
        queueFrameForControl(newResponseFrame(ControlFrame.S_8_RECEIVE_READY, false))
    }

    private fun handleRequestForState() {
        queueFrameForControl(newResponseFrame(ControlFrame.S_8_RECEIVE_READY_P, false))
    }

    private fun rejectUnsequencedFrame(incomingFrame: KissFrame) {
        logger.error("Rejecting frame with unexpected sequence number. Expected: {} Received {}", nextExpectedSendSequenceNumberFromPeer, incomingFrame.sendSequenceNumber())
        val frame = newResponseFrame(ControlFrame.S_8_REJECT, false)
        queueFrameForControl(frame)
    }

    private fun handleDisconnectRequest() {
        logger.trace("Disconnecting from $remoteCallsign")
        val frame = newResponseFrame(ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        queueFrameForControl(frame)
    }

    private fun ignoreFrame(incomingFrame: KissFrame) {
        logger.error("No handler for frame. Ignored. ${incomingFrame.toString()}")
    }

    /* Application Interface methods */
    private fun sendMessage(message: String) {
        val payloadChunks = chunkUpPayload(message)
        for (payloadChunk in payloadChunks) {
            val contentFrame = newResponseFrame(ControlFrame.INFORMATION_8, false)
            contentFrame.setPayloadMessage(payloadChunk)
            sequencedQueue.addFrameForSequencedTransmission(contentFrame)
        }
    }

    /* Factory methods */
    private fun newResponseFrame(frameType: ControlFrame, extended: Boolean): KissFrame {
        val newFrame = when (extended) {
            false -> KissFrameStandard()
            true -> KissFrameExtended()
        }
        newFrame.setDestCallsign(remoteCallsign)
        newFrame.setSourceCallsign(myCallsign)
        newFrame.setControlType(frameType)

        // Set the receive sequence number on frames that require it
        if (listOf(ControlFrame.INFORMATION_8, ControlFrame.INFORMATION_8_P,
                ControlFrame.INFORMATION_128, ControlFrame.INFORMATION_128_P,
                ControlFrame.S_8_RECEIVE_READY, ControlFrame.S_8_RECEIVE_READY_P,
                ControlFrame.S_8_RECEIVE_NOT_READY, ControlFrame.S_8_RECEIVE_NOT_READY_P,
                ControlFrame.S_8_REJECT, ControlFrame.S_8_REJECT_P,
                ControlFrame.S_8_SELECTIVE_REJECT, ControlFrame.S_8_SELECTIVE_REJECT_P,
                ControlFrame.S_128_RECEIVE_NOT_READY, ControlFrame.S_128_RECEIVE_READY_P,
                ControlFrame.S_128_RECEIVE_READY, ControlFrame.S_128_RECEIVE_READY_P,
                ControlFrame.S_128_REJECT, ControlFrame.S_128_REJECT_P,
                ControlFrame.S_128_SELECTIVE_REJECT, ControlFrame.S_128_SELECTIVE_REJECT_P).contains(frameType)) {
                newFrame.setReceiveSequenceNumber(nextExpectedSendSequenceNumberFromPeer)
        }
        return newFrame
    }

    /* State management */
    private fun incrementReceiveSequenceNumber() {
        val maxSeq = maxSequenceValue()
        if (nextExpectedSendSequenceNumberFromPeer < maxSeq) {
            nextExpectedSendSequenceNumberFromPeer++
        } else {
            nextExpectedSendSequenceNumberFromPeer = 0
        }
    }

    private fun maxSequenceValue(): Int {
        return when (controlMode) {
            ControlMode.MODULO_8 -> 7
            ControlMode.MODULO_128 -> 128
        }
    }

    /* Frame utility methods */
    /**
     * This method breaks a payload up into smaller chunks so that
     * the max receive size of a frame is not reached. Typically the
     * maximum frame size is 256 bytes, however, in the future we need
     * to implement the XID frame handler so that this value can be
     * agreed dynamically with the client TNC.
     */
    fun chunkUpPayload(payload: String): List<String> {
        var remainingPayload = payload
        var splitPayload = ArrayList<String>()
        // Break the playload into smaller parts
        while (remainingPayload.length > KissFrame.PAYLOAD_MAX) {
            splitPayload.add(remainingPayload.substring(0, KissFrame.PAYLOAD_MAX))
            remainingPayload = remainingPayload.substring(KissFrame.PAYLOAD_MAX, remainingPayload.length)
        }
        // Add any remaining payload data that falls within the maximum payload size
        splitPayload.add(remainingPayload)
        return splitPayload
    }

}