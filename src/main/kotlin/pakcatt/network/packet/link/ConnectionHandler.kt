package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import pakcatt.network.packet.kiss.ControlFrame
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissFrameExtended
import pakcatt.network.packet.kiss.KissFrameStandard
import pakcatt.network.packet.link.model.*
import java.util.*
import kotlin.collections.ArrayList

enum class ControlMode {
    MODULO_8, MODULO_128
}

class ConnectionHandler(val remoteCallsign: String,
                        val myCallsign: String,
                        val linkInterface: LinkInterface) {

    private val logger = LoggerFactory.getLogger(ConnectionHandler::class.java)
    private var controlMode = ControlMode.MODULO_8
    var transmitQueue = LinkedList<KissFrame>()
    var initialQueue = LinkedList<KissFrame>()

     /* Section 4.2.4 Frame Variables and Sequence Numbers, Beech et all */
     /* The send state variable contains the next sequential number to be assigned to the next transmitted I frame.
      * This variable is updated with the transmission of each I frame.*/
    private var nextSendSequenceNumber = 0

    /* The receive state variable contains the sequence number of the next expected received I frame.
     * This variable is updated upon the reception of an error-free I frame whose send sequence number
     * equals the present received state variable value. */
    private var nextExpectedReceiveSequenceNumber = 0

    /* The acknowledge state variable contains the sequence number of the last
     * frame acknowledged by its peer [V(A)-1 equals the N(S) of the last acknowledged I frame]. */
    private var lastSentSequenceNumberAcknowledged = 0

    fun handleIncomingFrame(incomingFrame: KissFrame) {
        when (incomingFrame.controlFrame()) {
            ControlFrame.U_SET_ASYNC_BALANCED_MODE_P -> handleConnectionRequest(incomingFrame) // Connection request
            ControlFrame.INFORMATION_8_P -> handleNumberedInformationFrame(incomingFrame, true) // Application info frame
            ControlFrame.INFORMATION_8 -> handleNumberedInformationFrame(incomingFrame, false) // Application info frame
            ControlFrame.S_8_RECEIVE_READY_P -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.S_8_RECEIVE_READY -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.S_8_REJECT -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.S_8_REJECT_P -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.U_REJECT -> handleIncomingAcknowledgement(incomingFrame)
            ControlFrame.U_DISCONNECT_P -> handleDisconnectRequest() // Disconnect request
            else -> ignoreFrame(incomingFrame)
        }
    }

    // Remove frames from our transmit queue that have been delivered / acknowledged
    // This method allows for one frame to be retransmitted, all other frames are removed.
    fun removeDeliveredFrames() {
        var deliveredFrames = LinkedList<KissFrame>()
        for (frame in transmitQueue) {
            // Always assume frames are acknowledged when they are below the last acknowledged sequence number sent to us
            if (frame.requiresAcknowledgement() && frame.sendSequenceNumber() < lastSentSequenceNumberAcknowledged) {
                logger.trace("Frame acknowledged up to {}, removing from transmit queue {}", lastSentSequenceNumberAcknowledged, frame.toString())
                deliveredFrames.add(frame)
            } else if (frame.requiresAcknowledgement() && frame.sendSequenceNumber() > lastSentSequenceNumberAcknowledged + 3) {
                // If the acknowledged sequence number has rolled over, remove up to 4 previous frames with higher numbers.
                logger.trace("Frame acknowledged up to {}, removing from transmit queue {}", lastSentSequenceNumberAcknowledged, frame.toString())
                deliveredFrames.add(frame)
            }
        }
        transmitQueue.removeAll(deliveredFrames)
    }

    fun queueMoreFramesForDelivery() {
        // Allow a limited number of frames in the transmit queue
        while (transmitQueue.size < 3 && initialQueue.isNotEmpty()) {
            // Set the send sequence number on frames that require it
            val frameForDelivery = initialQueue.removeFirst()
            if (frameForDelivery.requiresAcknowledgement()) {
                frameForDelivery.setSendSequenceNumber(nextSendSequenceNumber)
                logger.debug("Set a send sequence number of {} for frame {}", nextSendSequenceNumber, frameForDelivery.toString())
                incrementSendSequenceNumber()
            }
            transmitQueue.add(frameForDelivery)
        }
    }

    private fun queueFrameForTransmission(frame: KissFrame) {
        logger.trace("Queueing frame for transmission {}", frame.toString())
        initialQueue.add(frame)
    }

    /* Application interface */
    private fun handleNumberedInformationFrame(incomingFrame: KissFrame, pACKRequired: Boolean) {
        // Check that we expected this frame, and haven't missed any or mixed up the order
        if (incomingFrame.sendSequenceNumber() == nextExpectedReceiveSequenceNumber) {
            incrementReceiveSequenceNumber()
            handleIncomingAcknowledgement(incomingFrame)

            // Share the payload with any listening applications to process
            val  appResponse = linkInterface.getResponseForReceivedMessage(LinkRequest(incomingFrame.sourceCallsign(), incomingFrame.destCallsign(), incomingFrame.payloadDataString()))
            when (appResponse.responseType) {
                InteractionResponseType.SEND_TEXT -> acknowledgeAndSendMessage(appResponse.responseString(), pACKRequired)
                InteractionResponseType.ACK_ONLY -> acknowledgeBySendingReadyReceive(pACKRequired)
                InteractionResponseType.IGNORE -> logger.trace("Apps ignored frame: ${incomingFrame.toString()}")
            }
        } else {
            logger.error("Received an out of sequence information frame. Expected send sequence number: $nextExpectedReceiveSequenceNumber. Frame: ${incomingFrame.toString()}")
            realignSendSequenceNumber(incomingFrame)
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
        initialQueue = LinkedList<KissFrame>()
        transmitQueue = LinkedList<KissFrame>()
        nextExpectedReceiveSequenceNumber = 0
        nextSendSequenceNumber = 0
        lastSentSequenceNumberAcknowledged = 0
        val frame = newResponseFrame(ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        queueFrameForTransmission(frame)
    }

    private fun acknowledgeAndSendMessage(message: String, pACKRequired: Boolean) {
        sendMessage(message, pACKRequired)
    }
    private fun acceptIncomingConnectionWithMessage(message: String) {
        acceptIncomingConnection()
        sendMessage(message, false)
    }

    private fun handleIncomingAcknowledgement(incomingFrame: KissFrame) {
        // We expect the remote party to acknowledge the sequence number we last sent by sending us the
        // send sequence number they next expect from us.
        if (incomingFrame.receiveSequenceNumber() != nextSendSequenceNumber) {
            logger.error("Remote party $remoteCallsign is expecting to receive frame ${incomingFrame.receiveSequenceNumber()} next but we will send frame $nextSendSequenceNumber next.")
        }

        // If our record of our last acknowledged sent frame is already updated, then the remote party may be asking us to re-sync with an RR_P
        if ((incomingFrame.controlFrame() == ControlFrame.S_8_RECEIVE_READY_P
                    || incomingFrame.controlFrame() == ControlFrame.S_128_RECEIVE_READY_P)
                    && lastSentSequenceNumberAcknowledged == incomingFrame.receiveSequenceNumber()) {
            logger.debug("Received multiple of the same acknowledgement sequence number {}, sending an RR_P to re-sync.", lastSentSequenceNumberAcknowledged)
            acknowledgeBySendingReadyReceive(true)
        }

        // Keep a record of the next send sequence number our remote party expects us to send
        lastSentSequenceNumberAcknowledged = incomingFrame.receiveSequenceNumber()
    }

    private fun acknowledgeBySendingReadyReceive(pACKRequired: Boolean) {
        val frame = when (pACKRequired) {
            true -> newResponseFrame(ControlFrame.S_8_RECEIVE_READY_P, false)
            false -> newResponseFrame(ControlFrame.S_8_RECEIVE_READY, false)
        }
        queueFrameForTransmission(frame)
    }

    private fun realignSendSequenceNumber(incomingFrame: KissFrame) {
        logger.error("Re-aligning receive sequence number: ${incomingFrame.toString()}")
        nextExpectedReceiveSequenceNumber = incomingFrame.sendSequenceNumber()
        acknowledgeBySendingReadyReceive(true)
    }

    private fun handleDisconnectRequest() {
        logger.trace("Disconnecting from $remoteCallsign")
        val frame = newResponseFrame(ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        queueFrameForTransmission(frame)
    }

    private fun ignoreFrame(incomingFrame: KissFrame) {
        logger.error("No handler for frame. Ignored. ${incomingFrame.toString()}")
    }

    /* Application Interface methods */
    private fun sendMessage(message: String, pACKRequired: Boolean) {
        val payloadChunks = chunkUpPayload(message)
        for (payloadChunk in payloadChunks) {
            val frame = when (pACKRequired) {
                true -> newResponseFrame(ControlFrame.INFORMATION_8_P, false)
                false -> newResponseFrame(ControlFrame.INFORMATION_8, false)
            }
            frame.setPayloadMessage(payloadChunk)
            queueFrameForTransmission(frame)
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
                newFrame.setReceiveSequenceNumber(nextExpectedReceiveSequenceNumber)
        }

        return newFrame
    }

    /* State management */
    private fun incrementReceiveSequenceNumber() {
        val maxSeq = maxSequenceValue()
        if (nextExpectedReceiveSequenceNumber < maxSeq) {
            nextExpectedReceiveSequenceNumber++
        } else {
            nextExpectedReceiveSequenceNumber = 0
        }
    }

    private fun incrementSendSequenceNumber() {
        val maxSeq = maxSequenceValue()
        if (nextSendSequenceNumber < maxSeq) {
            nextSendSequenceNumber++
        } else {
            nextSendSequenceNumber = 0
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