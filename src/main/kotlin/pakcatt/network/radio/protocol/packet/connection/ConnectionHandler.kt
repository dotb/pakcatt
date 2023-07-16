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

enum class PFlagStatus {
    P_FLAG_CLEARED, P_FLAG_SET_BY_US, P_FLAG_SET_BY_THEM
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
    private var pFlagState = PFlagStatus.P_FLAG_CLEARED

    /* The receive state variable contains the sequence number of the next expected received I frame.
     * This variable is updated upon the reception of an error-free I frame whose send sequence number
     * equals the present received state variable value. */
    private var nextExpectedSendSequenceNumberFromPeer = 0

    fun handleIncomingFrame(incomingFrame: KissFrame) {
        updatePFlagBasedOnIncomingFrame(incomingFrame)
        when (incomingFrame.controlField()) {
            ControlField.U_SET_ASYNC_BALANCED_MODE -> handleIncomingConnectionRequest(incomingFrame)
            ControlField.U_SET_ASYNC_BALANCED_MODE_P -> handleIncomingConnectionRequest(incomingFrame)
            ControlField.INFORMATION_8 -> handleNumberedInformationFrame(incomingFrame)
            ControlField.INFORMATION_8_P -> handleNumberedInformationFrame(incomingFrame)
            ControlField.S_8_RECEIVE_READY -> handleIncomingAcknowledgement(incomingFrame)
            ControlField.S_8_RECEIVE_READY_P -> handleIncomingAckOrARequestForStatus(incomingFrame)
            ControlField.S_8_REJECT -> handleIncomingAcknowledgement(incomingFrame)
            ControlField.S_8_REJECT_P -> handleIncomingAcknowledgement(incomingFrame)
            ControlField.U_DISCONNECT_P -> handleDisconnectRequest()
            ControlField.U_REJECT -> handleIncomingAcknowledgement(incomingFrame)
            else -> ignoreFrame(incomingFrame)
        }
    }

    /**
     * This function is called to collect frames to be added
     * to the global delivery queue for delivery in the next over.
     * It should first queue control frames, then content frames, and
     * lastly make sure the first frame has the correct P/F flag set.
     */
    fun queueFramesForDelivery(globalDeliveryQueue: DeliveryQueue) {
        val indexOfFirstFrameThatWasAdded = globalDeliveryQueue.queueSize()
        val localQueueOfContentFrames = DeliveryQueue()
        val localQueueOfControlFrames = DeliveryQueue()
        // Collect content frames and then control frames to make sure we have the latest control frame
        queueContentFramesForDelivery(localQueueOfContentFrames)
        queueControlFramesForDelivery(localQueueOfControlFrames)
        // Add our collected frames to the global delivery queue
        globalDeliveryQueue.addFramesFromQueue(localQueueOfControlFrames)
        globalDeliveryQueue.addFramesFromQueue(localQueueOfContentFrames)
        // Lastly, make sure the P/F flag is set correctly for the first frame to be sent
        val indexOfLastFrameThatWasAdded = globalDeliveryQueue.queueSize() - 1
        updatePFlagForFirstFrameInDeliveryQueue(globalDeliveryQueue, indexOfFirstFrameThatWasAdded, indexOfLastFrameThatWasAdded)
    }

    /**
     * This function is called to get any control frame to be added
     * to the global delivery queue, so that it can be sent in the next over.
     */
    private fun queueControlFramesForDelivery(deliveryQueue: DeliveryQueue) {
        val controlFrame = nextQueuedControlFrame
        if (null != controlFrame) {
            controlFrame.setReceiveSequenceNumberIfRequired(nextExpectedSendSequenceNumberFromPeer)
            deliveryQueue.addFrame(controlFrame)
            nextQueuedControlFrame = null
            logger.trace("Added a control frame to the global delivery queue: {}", controlFrame)
        }
    }

    /**
     * This function is called to collect any content frames to be added
     * to the global delivery queue, for delivery in the next over.
     */
    private fun queueContentFramesForDelivery(deliveryQueue: DeliveryQueue) {
        // Deliver unnumbered frames that do not require an ACK
        val unnumberedFramesForDelivery = unnumberedQueue
        unnumberedQueue = ArrayList() // Reset the unnumberedQueue
        for (frame in unnumberedFramesForDelivery) {
            frame.setReceiveSequenceNumberIfRequired(nextExpectedSendSequenceNumberFromPeer)
            deliveryQueue.addFrame(frame)
            logger.trace("Added an unnumbered content frame to the global delivery queue: {}", frame)
        }

        // Deliver numbered frames that do require an ACK
        val numberedFramesForDelivery = sequencedQueue.getSequencedFramesForDelivery()
        if (numberedFramesForDelivery.isNotEmpty()) {
            for ((index, frame) in numberedFramesForDelivery.withIndex()) {
                // If this is the last frame to be delivered in this over, set the P flag.
                if (index >= numberedFramesForDelivery.size - 1) {
                    frame.setControlField(ControlField.INFORMATION_8_P, nextExpectedSendSequenceNumberFromPeer, frame.sendSequenceNumber())
                    // We'll expect the remote station to clear this flag, to ensure we get a Receive Ready to know when we can send more data.
                    pFlagState = PFlagStatus.P_FLAG_SET_BY_US
                } else {
                    frame.setControlField(ControlField.INFORMATION_8, nextExpectedSendSequenceNumberFromPeer, frame.sendSequenceNumber())
                }
                deliveryQueue.addFrame(frame)
                logger.trace("Added an numbered content frame to the global delivery queue: {}", frame)
            }
        }
    }

    /**
     * Update the P/F Flag for the first frame in the delivery queue, depending on the
     * state of pFlagState
     */
    private fun updatePFlagForFirstFrameInDeliveryQueue(deliveryQueue: DeliveryQueue, indexOfFirstFrameThatWasAdded: Int, indexOfLastFrameThatWasAdded: Int) {
        if (deliveryQueue.queueSize() > 0 && deliveryQueue.queueSize() > indexOfFirstFrameThatWasAdded) {
            when (pFlagState) {
                PFlagStatus.P_FLAG_CLEARED -> deliveryQueue.unsetPFlagForFrameAtIndex(indexOfFirstFrameThatWasAdded)
                PFlagStatus.P_FLAG_SET_BY_US -> deliveryQueue.setPFlagForFrameAtIndex(indexOfLastFrameThatWasAdded)
                PFlagStatus.P_FLAG_SET_BY_THEM -> {
                    logger.trace("The remote station raised the P Flag. Making sure the first packet we send has the F bit set and clearing our raised P Flag.")
                    deliveryQueue.setPFlagForFrameAtIndex(indexOfFirstFrameThatWasAdded)
                    pFlagState = PFlagStatus.P_FLAG_CLEARED
                }
            }
        }
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

    private fun handleIncomingConnectionRequest(incomingFrame: KissFrame) {
        // Start with a  clean slate
        resetConnection()
        // Establish a fresh connection
        handleConnectionRequest(incomingFrame)
        // But we need to keep the state of any P Flag, add it back to our state
        updatePFlagBasedOnIncomingFrame(incomingFrame)
    }

    private fun handleNumberedInformationFrame(incomingFrame: KissFrame) {
        // Check that we expected this frame, and haven't missed any or mixed up the order
        logger.trace("Handling numbered information for frame: {}", incomingFrame.toString())
        if (isConnected()) {
            updateOurSendSequenceNumberBasedOnFrame(incomingFrame)
            if (nextExpectedSendSequenceNumberFromPeer == nextExpectedSendSequenceNumberFromPeer) {
                incrementOurReceiveSequenceNumber()
                // Share the payload with any listening applications to process
                val appResponse = linkInterface.getResponseForReceivedMessage(
                    AppRequest(
                        incomingFrame.channelIdentifier,
                        incomingFrame.sourceCallsign(),
                        stringUtils.formatCallsignRemoveSSID(incomingFrame.sourceCallsign()),
                        incomingFrame.destCallsign(),
                        incomingFrame.payloadDataString()
                    )
                )
                when (appResponse.responseType) {
                    ResponseType.ACK_WITH_TEXT -> queueMessageForDelivery(ControlField.INFORMATION_8, appResponse.responseString())
                    ResponseType.ACK_ONLY -> sendReceiveReady()
                    ResponseType.IGNORE -> logger.trace("Apps ignored frame: $incomingFrame")
                    ResponseType.DISCONNECT -> logger.error("NEED TO DO THIS") //TODO

                }
            } else {
                logger.error("Received a frame that is out of sequence. nextExpectedSendSequenceNumberFromPeer is {} but got {} : {}", nextExpectedSendSequenceNumberFromPeer, nextExpectedSendSequenceNumberFromPeer, incomingFrame)
                rejectUnsequencedFrame(incomingFrame)
            }
        } else {
            logger.error("Remote station sent us an information frame but we're not connected: {}", incomingFrame)
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
            incomingFrame.payloadDataString()))
        when (appResponse.responseType) {
            ResponseType.ACK_ONLY -> acceptIncomingConnection()
            ResponseType.ACK_WITH_TEXT -> acceptIncomingConnectionWithMessage(appResponse.responseString())
            ResponseType.IGNORE -> logger.trace("Ignored connection request from: $remoteCallsign to :$myCallsign on Chan: $channelIdentifier")
            ResponseType.DISCONNECT -> logger.error("NEEDS TO BE IMPLEMENTED") //TODO
        }
    }

    private fun acceptIncomingConnection() {
        logger.info("Accepting connection from remote party: $remoteCallsign local party: $myCallsign on Chan: $channelIdentifier")
        resetConnection()
        connectionStatus = ConnectionStatus.CONNECTED
        val frame = newResponseFrame(ControlField.U_UNNUMBERED_ACKNOWLEDGE, false)
        queueFrameForControl(frame)
        setRemoteStationAsReadyToReceiveDataFromUs() // The remote station is ready. Expire the delivery time so that we deliver the next batch of frames.
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
        pFlagState = PFlagStatus.P_FLAG_CLEARED
    }

    private fun handleIncomingAcknowledgement(incomingFrame: KissFrame) {
        logger.trace("Handling ack frame: {}", incomingFrame.toString())
        if (isConnected()) {
            updateOurSendSequenceNumberBasedOnFrame(incomingFrame)
            setRemoteStationAsReadyToReceiveDataFromUs() // The remote station is ready. Expire the delivery time so that we deliver the next batch of frames.
        } else {
            logger.error("A remote station sent us an ACK but we're not connected to them: {}", incomingFrame.toString())
        }
    }

    /**
     * This incoming frame is due to one of two states:
     * 1) We raised the P Flag, and the remote station is clearing it by setting the F bit
     * 2) We've not raised the P Flag, and the remote station is requesting us to sync and
     *    clear their raised flag by sending the F bit
     *
     * 4.3.2.1 Receive Ready (RR) Command and Response
     * The status of the TNC at the other end of the link can be requested by sending an RR
     * command frame with the P-bit set to one.
     */
    private fun handleIncomingAckOrARequestForStatus(incomingFrame: KissFrame) {
        if (isConnected()) {
            handleIncomingAcknowledgement(incomingFrame)
            if (PFlagStatus.P_FLAG_SET_BY_THEM == pFlagState) {
                // We're in state 2 - send RR. The P flag will get set on the way out.
                logger.trace("We've detected that the remote station has raised the P flag, asking us to sync.")
                sendReceiveReady()
            }
        } else {
            logger.error("A remote station sent us an ACK or request for status but we're not connected to them: {}", incomingFrame.toString())
        }
    }

    private fun sendReceiveReady() {
        val newRRFrame = newResponseFrame(ControlField.S_8_RECEIVE_READY, false)
        queueFrameForControl(newRRFrame)
        logger.trace("Queueing a Receive Ready Frame: {}", newRRFrame)
    }

    private fun rejectUnsequencedFrame(incomingFrame: KissFrame) {
        logger.error("Rejecting frame with unexpected sequence number. Expected: {} Received {}", nextExpectedSendSequenceNumberFromPeer, incomingFrame.sendSequenceNumber())
        val frame = newResponseFrame(ControlField.S_8_REJECT, false)
        queueFrameForControl(frame)
        setRemoteStationAsReadyToReceiveDataFromUs() // The remote station is ready. Expire the delivery time so that we deliver the next batch of frames.
    }

    /**
     * Handle a disconnect by resetting the connection, clearing out the
     * delivery queues, then adding a last ack control message to confirm the
     * disconnect with the remote station.
     */
    private fun handleDisconnectRequest() {
        logger.trace("Disconnecting from $remoteCallsign")
        resetConnection()
        val frame = newResponseFrame(ControlField.U_UNNUMBERED_ACKNOWLEDGE, false)
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
    private fun incrementOurReceiveSequenceNumber() {
        val maxSeq = maxSequenceValue()
        if (nextExpectedSendSequenceNumberFromPeer < maxSeq) {
            nextExpectedSendSequenceNumberFromPeer++
        } else {
            nextExpectedSendSequenceNumberFromPeer = 0
        }
    }

    private fun updateOurSendSequenceNumberBasedOnFrame(incomingFrame: KissFrame) {
        sequencedQueue.updateOurSendSequenceNumberBasedOnIncomingFrame(incomingFrame)
    }

    private fun setRemoteStationAsReadyToReceiveDataFromUs() {
        sequencedQueue.remoteStationIsReadyExpireDeliveryTimer()
    }

    private fun maxSequenceValue(): Int {
        return when (controlMode) {
            ControlMode.MODULO_8 -> 7
            ControlMode.MODULO_128 -> 128
        }
    }

    private fun updatePFlagBasedOnIncomingFrame(incomingFrame: KissFrame) {
        if (incomingFrame.isPFlagSet()) {
            pFlagState = when (pFlagState) {
                PFlagStatus.P_FLAG_CLEARED -> {
                    /* The remote station is raising the P-Flag. We need to clear it.
                    * Setting this flag here will cause our first packet's F bit to be
                    * set just before it is given to the global delivery queue. */
                    logger.trace("The remote station set the P Flag. We need to respond by setting the F bit on the first packet we send back to them.")
                    PFlagStatus.P_FLAG_SET_BY_THEM
                }
                PFlagStatus.P_FLAG_SET_BY_US -> {
                    // Remote station is clearing our raised P-Flag (we hope!)
                    logger.trace("The remote station cleared our raised P Flag.")
                    PFlagStatus.P_FLAG_CLEARED
                }
                PFlagStatus.P_FLAG_SET_BY_THEM -> {
                    // We should never get here
                    logger.error("The remote station set the P Flag but we've already recorded it as being set by them. Duplicated frame, maybe?. We're going to clear it.")
                    PFlagStatus.P_FLAG_CLEARED
                }
            }
        } else {
            pFlagState = when (pFlagState) {
                PFlagStatus.P_FLAG_CLEARED -> {
                    // Do nothing. We're all good
                    logger.trace("Our P Flag is not raised, and the remote station hasn't raised it either. All is well.")
                    PFlagStatus.P_FLAG_CLEARED
                }
                PFlagStatus.P_FLAG_SET_BY_US -> {
                    logger.error("Weird! We raised the P Flag but the remote station didn't clear it. We'll clear it anyway")
                    PFlagStatus.P_FLAG_CLEARED
                }
                PFlagStatus.P_FLAG_SET_BY_THEM -> {
                    // We should never get here
                    logger.error("The remote station had previously set the P Flag and by now we should have cleared it. Strange. We'll clear it now.")
                    PFlagStatus.P_FLAG_CLEARED
                }
            }
        }
    }

    private fun isConnected(): Boolean {
        return ConnectionStatus.CONNECTED == connectionStatus
    }

    /* Frame utility methods */
    /**
     * This method breaks a payload up into smaller chunks so that
     * the max receive size of a frame is not reached. Typically the
     * maximum frame size is 256 bytes, however, in the future we need
     * to implement the XID frame handler so that this value can be
     * agreed dynamically with the client TNC.
     */
    private fun chunkUpPayload(payload: String): List<String> {
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