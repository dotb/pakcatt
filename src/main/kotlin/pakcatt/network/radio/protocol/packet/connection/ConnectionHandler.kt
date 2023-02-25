package pakcatt.network.radio.protocol.packet.connection

import org.slf4j.LoggerFactory
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.ResponseType
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.KissFrameExtended
import pakcatt.network.radio.kiss.model.KissFrameStandard
import pakcatt.network.radio.kiss.queue.DeliveryQueue
import pakcatt.network.radio.protocol.packet.LinkInterface
import pakcatt.util.StringUtils
import kotlin.collections.ArrayList

enum class ControlMode {
    MODULO_8, MODULO_128
}

enum class ConnectionStatus {
    CONNECTED, DISCONNECTED
}

class ConnectionHandler(
    private val channelIdentifier: String, // The channel identifier (TNC) to which this handler is connected.
    private val remoteCallsign: String,
    private val myCallsign: String,
    private val linkInterface: LinkInterface,
    private val frameSizeMax: Int,
    framesPerOver: Int,
    maxDeliveryAttempts: Int,
    deliveryRetryTimeSeconds: Int) {

    private val logger = LoggerFactory.getLogger(ConnectionHandler::class.java)
    private val stringUtils = StringUtils()
    private var controlMode = ControlMode.MODULO_8
    private var nextQueuedControlFrame: KissFrame? = null
    private var unnumberedQueue = ArrayList<KissFrame>()
    private var sequencedQueue = SequencedQueue(framesPerOver, maxDeliveryAttempts, deliveryRetryTimeSeconds)
    private var connectionStatus = ConnectionStatus.DISCONNECTED

    /* The receive state variable contains the sequence number of the next expected received I frame.
     * This variable is updated upon the reception of an error-free I frame whose send sequence number
     * equals the present received state variable value. */
    private var nextExpectedSendSequenceNumberFromPeer = 0

    fun handleIncomingFrame(incomingFrame: KissFrame) {
        when {
            incomingFrame.controlField() == ControlField.U_SET_ASYNC_BALANCED_MODE_P -> {
                handleConnectionRequest(incomingFrame)
            }
            connectionStatus == ConnectionStatus.CONNECTED -> {
                when (incomingFrame.controlField()) {
                    ControlField.INFORMATION_8 -> handleNumberedInformationFrame(incomingFrame) // Application info frame
                    ControlField.INFORMATION_8_P -> handleNumberedInformationFrame(incomingFrame) // Application info frame
                    ControlField.S_8_RECEIVE_READY -> handleIncomingAcknowledgement(incomingFrame)
                    ControlField.S_8_RECEIVE_READY_P -> handleIncomingAcknowledgement(incomingFrame)
                    ControlField.S_8_REJECT -> handleIncomingAcknowledgement(incomingFrame)
                    ControlField.S_8_REJECT_P -> handleIncomingAcknowledgement(incomingFrame)
                    ControlField.U_DISCONNECT_P -> handleDisconnectRequest() // Disconnect request
                    ControlField.U_REJECT -> resetConnection() // FRMR is unrecoverable. We reset the connection.
                    else -> ignoreFrame(incomingFrame)
                }
            }
            else -> {
                logger.trace("Not connected:\t {}", incomingFrame)
            }
        }
    }

    fun deliverQueuedControlFrame(deliveryQueue: DeliveryQueue): Int {
        val controlFrame = nextQueuedControlFrame
        return if (null != controlFrame) {
            controlFrame.setReceiveSequenceNumberIfRequired(nextExpectedSendSequenceNumberFromPeer)
            deliveryQueue.addFrame(controlFrame)
            nextQueuedControlFrame = null
            1
        } else {
            0
        }
    }

    fun deliverContentFrames(deliveryQueue: DeliveryQueue): Int {
        var deliveryCount = 0

        // Deliver unnumbered frames that do not require an ACK
        val unnumberedFramesForDelivery = unnumberedQueue
        unnumberedQueue = ArrayList() // Reset the unnumberedQueue
        for (frame in unnumberedFramesForDelivery) {
            frame.setReceiveSequenceNumberIfRequired(nextExpectedSendSequenceNumberFromPeer)
            deliveryQueue.addFrame(frame)
            deliveryCount++
        }

        // Deliver numbered frames that do require an ACK
        val numberedFramesForDelivery = sequencedQueue.getSequencedFramesForDelivery()
        if (numberedFramesForDelivery.isNotEmpty()) {
            for ((index, frame) in numberedFramesForDelivery.withIndex()) {

                // If this is the last frame to be delivered in this over, set the P flag.
                if (index >= numberedFramesForDelivery.size - 1 && !sequencedQueue.isAtEndOfMessageDelivery()) {
                    frame.setControlField(ControlField.INFORMATION_8_P, nextExpectedSendSequenceNumberFromPeer, frame.sendSequenceNumber())
                } else {
                    frame.setControlField(ControlField.INFORMATION_8, nextExpectedSendSequenceNumberFromPeer, frame.sendSequenceNumber())
                }

                deliveryQueue.addFrame(frame)
                deliveryCount++
            }

            // If we're at the end of a message, transmit READY_RECEIVE_P
            if (sequencedQueue.isAtEndOfMessageDelivery()) {
                val frame = newResponseFrame(ControlField.S_8_RECEIVE_READY_P, false)
                frame.setReceiveSequenceNumberIfRequired(nextExpectedSendSequenceNumberFromPeer)
                deliveryQueue.addFrame(frame)
                deliveryCount++
            }
        }
        return deliveryCount
    }

    fun queueMessageForDelivery(controlField: ControlField, message: String) {
        val payloadChunks = chunkUpPayload(message)
        for (payloadChunk in payloadChunks) {
            val contentFrame = newResponseFrame(controlField, false)
            contentFrame.setPayloadMessage(payloadChunk)
            if (contentFrame.requiresSendSequenceNumber()) {
                sequencedQueue.addFrameForSequencedTransmission(contentFrame)
            } else {
                unnumberedQueue.add(contentFrame)
            }
        }
    }

    private fun queueFrameForControl(frame: KissFrame) {
        nextQueuedControlFrame = frame
    }

    private fun handleNumberedInformationFrame(incomingFrame: KissFrame) {
        // Check that we expected this frame, and haven't missed any or mixed up the order
        if (incomingFrame.sendSequenceNumber() == nextExpectedSendSequenceNumberFromPeer) {
            incrementReceiveSequenceNumber()
            handleIncomingAcknowledgement(incomingFrame)

            // Share the payload with any listening applications to process
            val  appResponse = linkInterface.getResponseForReceivedMessage(AppRequest(incomingFrame.channelIdentifier,
                                                                            incomingFrame.sourceCallsign(),
                                                                            stringUtils.formatCallsignRemoveSSID(incomingFrame.sourceCallsign()),
                                                                            incomingFrame.destCallsign(),
                                                                            incomingFrame.payloadDataString(),
                                                                            true))
            when (appResponse.responseType) {
                ResponseType.ACK_WITH_TEXT -> queueMessageForDelivery(ControlField.INFORMATION_8, appResponse.responseString())
                ResponseType.ACK_ONLY -> sendAcknowlegeAndReadyForReceive()
                ResponseType.IGNORE -> logger.trace("Apps ignored frame: ${incomingFrame.toString()}")
            }
        } else {
            rejectUnsequencedFrame(incomingFrame)
        }
    }

    /**
     * Iterate the available applications and ask them if anyone wants us to establish a connection for this incoming frame.
     */
    private fun handleConnectionRequest(incomingFrame: KissFrame) {
        val appResponse = linkInterface.getDecisionOnConnectionRequest(AppRequest(incomingFrame.channelIdentifier,
                                                                        incomingFrame.sourceCallsign(),
                                                                        stringUtils.formatCallsignRemoveSSID(incomingFrame.sourceCallsign()),
                                                                        incomingFrame.destCallsign(),
                                                                        incomingFrame.payloadDataString(),
                                                                        true))
        when (appResponse.responseType) {
            ResponseType.ACK_ONLY -> acceptIncomingConnection()
            ResponseType.ACK_WITH_TEXT -> acceptIncomingConnectionWithMessage(appResponse.responseString())
            ResponseType.IGNORE -> logger.trace("Ignored connection request from: $remoteCallsign to :$myCallsign on Chan: $channelIdentifier")
        }
    }

    private fun acceptIncomingConnection() {
        logger.info("Accepting connection from remote party: $remoteCallsign local party: $myCallsign on Chan: $channelIdentifier")
        resetConnection()
        connectionStatus = ConnectionStatus.CONNECTED
        val frame = newResponseFrame(ControlField.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        queueFrameForControl(frame)
    }

    private fun acceptIncomingConnectionWithMessage(message: String) {
        acceptIncomingConnection()
        queueMessageForDelivery(ControlField.INFORMATION_8, message)
    }

    // Reset sequence state
    private fun resetConnection() {
        logger.debug("Resetting connection")
        controlMode = ControlMode.MODULO_8
        sequencedQueue.reset()
        unnumberedQueue = ArrayList<KissFrame>()
        nextQueuedControlFrame = null
        nextExpectedSendSequenceNumberFromPeer = 0
        connectionStatus = ConnectionStatus.DISCONNECTED
    }

    private fun handleIncomingAcknowledgement(incomingFrame: KissFrame) {
        // If our record of our last acknowledged sent frame is already updated, then the remote party may be asking us to re-sync with an RECEIVE_READY_P
        if (sequencedQueue.updateSequenceNumbersAndCheckIsDuplicate(incomingFrame)
            && (incomingFrame.controlField() == ControlField.S_8_RECEIVE_READY_P
                    || incomingFrame.controlField() == ControlField.S_128_RECEIVE_READY_P)) {
            logger.debug("Received multiple of the same acknowledgement sequence number. Sending an Ready_Receive_P to re-sync.")
            handleRequestForState()
        }
    }

    private fun sendAcknowlegeAndReadyForReceive() {
        queueFrameForControl(newResponseFrame(ControlField.S_8_RECEIVE_READY, false))
    }

    private fun handleRequestForState() {
        queueFrameForControl(newResponseFrame(ControlField.S_8_RECEIVE_READY_P, false))
    }

    private fun rejectUnsequencedFrame(incomingFrame: KissFrame) {
        logger.error("Rejecting frame with unexpected sequence number. Expected: {} Received {}", nextExpectedSendSequenceNumberFromPeer, incomingFrame.sendSequenceNumber())
        val frame = newResponseFrame(ControlField.S_8_REJECT, false)
        queueFrameForControl(frame)
    }

    /**
     * Handle a disconnect by resetting the connection, clearing out the
     * delivery queues, then adding a last ack control message to confirm the
     * disconnect with the remote station.
     */
    private fun handleDisconnectRequest() {
        logger.trace("Disconnecting from $remoteCallsign")
        resetConnection()
        val frame = newResponseFrame(ControlField.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        queueFrameForControl(frame)
    }

    private fun ignoreFrame(incomingFrame: KissFrame) {
        logger.error("No handler for frame. Ignored. ${incomingFrame.toString()}")
    }

    /* Factory methods */
    private fun newResponseFrame(fieldType: ControlField, extended: Boolean): KissFrame {
        val newFrame = when (extended) {
            false -> KissFrameStandard()
            true -> KissFrameExtended()
        }
        newFrame.setDestCallsign(remoteCallsign)
        newFrame.setSourceCallsign(myCallsign)
        // Set the control type now. The receive and send sequence numbers are updated just before it's transmitted.
        newFrame.setControlField(fieldType)
        newFrame.channelIdentifier = channelIdentifier
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