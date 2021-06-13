package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import pakcatt.network.packet.kiss.*
import pakcatt.network.packet.link.model.*
import kotlin.collections.ArrayList

enum class ControlMode {
    MODULO_8, MODULO_128
}

class ConnectionHandler(private val remoteCallsign: String,
                        private val myCallsign: String,
                        private val linkInterface: LinkInterface,
                        private val frameSizeMax: Int,
                        framesPerOver: Int,
                        maxDeliveryAttempts: Int) {

    private val logger = LoggerFactory.getLogger(ConnectionHandler::class.java)
    private var controlMode = ControlMode.MODULO_8
    private var nextQueuedControlFrame: KissFrame? = null
    private var unnumberedQueue = ArrayList<KissFrame>()
    private var sequencedQueue = SequencedQueue(framesPerOver, maxDeliveryAttempts)

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
            ControlFrame.S_8_REJECT_P -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.U_DISCONNECT_P -> handleDisconnectRequest() // Disconnect request
            ControlFrame.U_REJECT -> resetConnection() // FRMR is unrecoverable. We reset the connection.
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

    fun deliverContentFrames(kissService: KissService): Int {
        var deliveryCount = 0

        // Deliver unnumbered frames that do not require an ACK
        val unnumberedFramesForDelivery = unnumberedQueue
        unnumberedQueue = ArrayList() // Reset the unnumberedQueue
        for (frame in unnumberedFramesForDelivery) {
            kissService.transmitFrame(frame)
            deliveryCount++
        }

        // Deliver numbered frames that do require an ACK
        val numberedFramesForDelivery = sequencedQueue.getSequencedFramesForDelivery()
        if (numberedFramesForDelivery.isNotEmpty()) {
            for ((index, frame) in numberedFramesForDelivery.withIndex()) {

                // If this is the last frame to be delivered in this over, set the P flag.
                if (index >= numberedFramesForDelivery.size - 1 && !sequencedQueue.isAtEndOfMessageDelivery()) {
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

    fun queueMessageForDelivery(controlFrame: ControlFrame, message: String) {
        val payloadChunks = chunkUpPayload(message)
        for (payloadChunk in payloadChunks) {
            val contentFrame = newResponseFrame(controlFrame, false)
            contentFrame.setPayloadMessage(payloadChunk)
            if (contentFrame.requiresAcknowledgement()) {
                sequencedQueue.addFrameForSequencedTransmission(contentFrame)
            } else {
                unnumberedQueue.add(contentFrame)
            }
        }
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
                ResponseType.ACK_WITH_TEXT -> queueMessageForDelivery(ControlFrame.INFORMATION_8, appResponse.responseString())
                ResponseType.ACK_ONLY -> sendAcknowlegeAndReadyForReceive()
                ResponseType.IGNORE -> logger.trace("Apps ignored frame: ${incomingFrame.toString()}")
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
            ResponseType.ACK_ONLY -> acceptIncomingConnection()
            ResponseType.ACK_WITH_TEXT -> acceptIncomingConnectionWithMessage(appResponse.responseString())
            ResponseType.IGNORE -> logger.trace("Ignored connection request from: $remoteCallsign to :$myCallsign")
        }
    }

    private fun acceptIncomingConnection() {
        resetConnection()
        logger.info("Accepting connection from remote party: $remoteCallsign local party: $myCallsign")
        val frame = newResponseFrame(ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        queueFrameForControl(frame)
    }

    private fun acceptIncomingConnectionWithMessage(message: String) {
        acceptIncomingConnection()
        queueMessageForDelivery(ControlFrame.INFORMATION_8, message)
    }

    // Reset sequence state
    private fun resetConnection() {
        logger.debug("Resetting connection")
        controlMode = ControlMode.MODULO_8
        sequencedQueue.reset()
        unnumberedQueue = ArrayList<KissFrame>()
        nextQueuedControlFrame = null
        nextExpectedSendSequenceNumberFromPeer = 0
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
        val payloadSize = frameSizeMax - KissFrame.SIZE_HEADERS
        var remainingPayload = payload
        var splitPayload = ArrayList<String>()
        // Break the playload into smaller parts
        while (remainingPayload.length > payloadSize) {
            splitPayload.add(remainingPayload.substring(0, payloadSize))
            remainingPayload = remainingPayload.substring(payloadSize, remainingPayload.length)
        }
        // Add any remaining payload data that falls within the maximum payload size
        splitPayload.add(remainingPayload)
        return splitPayload
    }

}