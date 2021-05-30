package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissFrameExtended
import pakcatt.network.packet.kiss.KissFrameStandard

class ConnectionHandler(val fromCallsign: String, val toCallsign: String, val networkInterface: NetworkInterface) {

    private val logger = LoggerFactory.getLogger(ConnectionHandler::class.java)


     /* Section 4.2.4 Frame Variables and Sequence Numbers, Beech et all */
     /* The send state variable contains the next sequential number to be assigned to the next transmitted I frame.
      * This variable is updated with the transmission of each I frame.*/
    private var nextSendSequenceNumber = 0

    /* The acknowledge state variable exists within the TNC and is never sent.
     * It contains the sequence number of the last frame acknowledged by its peer
     * [V(A)-1 equals the N(S) of the last acknowledged I frame]. */
    private var lastSentSequenceNumberAcknowleged = 0

    /* The receive state variable contains the sequence number of the next expected received I frame.
     * This variable is updated upon the reception of an error-free I frame whose send sequence number
     * equals the present received state variable value. */
    private var nextExpectedReceiveSequenceNumber = 0


    fun handleIncomingFrame(incomingFrame: KissFrame) {
        when (incomingFrame.controlFrame()) {
            KissFrame.ControlFrame.U_SET_ASYNC_BALANCED_MODE_P -> handleConnectionRequest(incomingFrame) // Connection request
            KissFrame.ControlFrame.I_8_P -> handleNumberedInformationFrame(incomingFrame) // Application info frame
            KissFrame.ControlFrame.S_8_RECEIVE_READY_P -> handleRequestForRXState(incomingFrame) // Request for our RX state
            KissFrame.ControlFrame.U_DISCONNECT_P -> handleDisconnectRequest(incomingFrame) // Disconnect request
            else -> ignoreFrame(incomingFrame)
        }
    }

    /* Application interface */
    private fun handleNumberedInformationFrame(incomingFrame: KissFrame) {
        logger.debug("Information frame: ${incomingFrame.toString()}")
        if (incomingFrame.sendSequenceNumber() == nextExpectedReceiveSequenceNumber) {
            nextExpectedReceiveSequenceNumber++
            acknowlegeWithSendingReadyReceive(incomingFrame)
        } else {
            logger.error("Received an out of sequence information frame. Expected send sequence number: $nextExpectedReceiveSequenceNumber. Frame: ${incomingFrame.toString()}")
        }
    }

    /* Link layer handlers */
    private fun handleConnectionRequest(incomingFrame: KissFrame) {
        logger.info("Connecting to $fromCallsign")
        val frame = newResponseFrame(KissFrame.ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        networkInterface.queueFrameForDelivery(frame)
    }

    private fun handleRequestForRXState(incomingFrame: KissFrame) {
        val frame = newResponseFrame(KissFrame.ControlFrame.S_8_RECEIVE_READY_P, false)
        frame.setReceiveSequenceNumber(nextExpectedReceiveSequenceNumber)
        networkInterface.queueFrameForDelivery(frame)
    }

    private fun acknowlegeWithSendingReadyReceive(incomingFrame: KissFrame) {
        val frame = newResponseFrame(KissFrame.ControlFrame.S_8_RECEIVE_READY, false)
        frame.setReceiveSequenceNumber(nextExpectedReceiveSequenceNumber)
        networkInterface.queueFrameForDelivery(frame)
    }

    private fun handleDisconnectRequest(incomingFrame: KissFrame) {
        logger.info("Disconnecting from $fromCallsign")
        val frame = newResponseFrame(KissFrame.ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        networkInterface.queueFrameForDelivery(frame)
    }

    private fun ignoreFrame(incomingFrame: KissFrame) {
        logger.error("No handler for frame. Ignored. ${incomingFrame.toString()}")
        networkInterface.closeConnection(this)
    }

    /* Factory methods */
    private fun newResponseFrame(frameType: KissFrame.ControlFrame, extended: Boolean): KissFrame {
        val newFrame = when (extended) {
            false -> KissFrameStandard()
            true -> KissFrameExtended()
        }
        newFrame.setDestCallsign(fromCallsign)
        newFrame.setSourceCallsign(toCallsign)
        newFrame.setControlType(frameType)
        return newFrame
    }

}