package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import pakcatt.application.shared.AppResponse
import pakcatt.application.shared.PacCattApp
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissFrameExtended
import pakcatt.network.packet.kiss.KissFrameStandard

class ConnectionHandler(val remoteCallsign: String,
                        val myCallsign: String,
                        val networkInterface: NetworkInterface,
                        val applications: List<PacCattApp>) {

    private val logger = LoggerFactory.getLogger(ConnectionHandler::class.java)

    enum class ControlMode {
        MODULO_8, MODULO_128
    }

    private var controlMode = ControlMode.MODULO_8

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
    private var lastSentSequenceNumberAcknowleged = 0


    fun handleIncomingFrame(incomingFrame: KissFrame) {
        when (incomingFrame.controlFrame()) {
            KissFrame.ControlFrame.U_SET_ASYNC_BALANCED_MODE_P -> handleConnectionRequest(incomingFrame) // Connection request
            KissFrame.ControlFrame.I_8_P -> handleNumberedInformationFrame(incomingFrame, true) // Application info frame
            KissFrame.ControlFrame.I_8 -> handleNumberedInformationFrame(incomingFrame, false) // Application info frame
            KissFrame.ControlFrame.S_8_RECEIVE_READY_P -> acknowlegeBySendingReadyReceive(incomingFrame, true) // Request for our RX state
            KissFrame.ControlFrame.S_8_RECEIVE_READY -> handleAcknowlegement(incomingFrame)
            KissFrame.ControlFrame.U_DISCONNECT_P -> handleDisconnectRequest(incomingFrame) // Disconnect request
            else -> ignoreFrame(incomingFrame)
        }
    }

    /* Application interface */
    private fun handleNumberedInformationFrame(incomingFrame: KissFrame, pACKRequired: Boolean) {
        logger.debug("Information frame: ${incomingFrame.toString()}")
        if (incomingFrame.sendSequenceNumber() == nextExpectedReceiveSequenceNumber) {
            incrementReceiveSequenceNumber()

            // Share the payload with any listening applications to process
            for (app in applications) {
                val appResponse = app.handleReceivedMessage(incomingFrame.sourceCallsign(), incomingFrame.destCallsign(), incomingFrame.payloadDataString())
                when (appResponse.responseType) {
                    AppResponse.ResponseType.TEXT -> sendMessage(appResponse.message)
                    AppResponse.ResponseType.ACK_ONLY -> acknowlegeBySendingReadyReceive(incomingFrame, pACKRequired)
                    AppResponse.ResponseType.IGNORE -> logger.debug("App ignored frame: ${incomingFrame.toString()}")
                }
            }
        } else {
            logger.error("Received an out of sequence information frame. Expected send sequence number: $nextExpectedReceiveSequenceNumber. Frame: ${incomingFrame.toString()}")
        }
    }

    /* Link layer handlers */
    private fun handleConnectionRequest(incomingFrame: KissFrame) {
        logger.info("Connecting to remote party: $remoteCallsign local party: $myCallsign")

        // Reset sequence state
        controlMode = ControlMode.MODULO_8
        nextExpectedReceiveSequenceNumber = 0
        nextSendSequenceNumber = 0
        lastSentSequenceNumberAcknowleged = 0

        // Acknowledge the connection
        val frame = newResponseFrame(KissFrame.ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        networkInterface.queueFrameForDelivery(frame)
    }

    private fun acknowlegeBySendingReadyReceive(incomingFrame: KissFrame, pACKRequired: Boolean) {
        val frame = when (pACKRequired) {
            true -> newResponseFrame(KissFrame.ControlFrame.S_8_RECEIVE_READY_P, false)
            false -> newResponseFrame(KissFrame.ControlFrame.S_8_RECEIVE_READY, false)
        }
        networkInterface.queueFrameForDelivery(frame)
    }

    private fun handleAcknowlegement(incomingFrame: KissFrame) {
        lastSentSequenceNumberAcknowleged == incomingFrame.receiveSequenceNumber()
    }

    private fun handleDisconnectRequest(incomingFrame: KissFrame) {
        logger.info("Disconnecting from $remoteCallsign")
        val frame = newResponseFrame(KissFrame.ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        networkInterface.queueFrameForDelivery(frame)
        networkInterface.closeConnection(this)
    }

    private fun ignoreFrame(incomingFrame: KissFrame) {
        logger.error("No handler for frame. Ignored. ${incomingFrame.toString()}")
        networkInterface.closeConnection(this)
    }

    /* Application Interface methods */
    private fun sendMessage(message: String) {
        logger.debug("Sending message to: $remoteCallsign from: $myCallsign message: $message")
        val messageWithEOL = "$message\n\r" // And EOL characters
        val frame = newResponseFrame(KissFrame.ControlFrame.I_8, false)
        frame.setPayloadMessage(messageWithEOL)
        networkInterface.queueFrameForDelivery(frame)
    }

    /* Factory methods */
    private fun newResponseFrame(frameType: KissFrame.ControlFrame, extended: Boolean): KissFrame {
        val newFrame = when (extended) {
            false -> KissFrameStandard()
            true -> KissFrameExtended()
        }
        newFrame.setDestCallsign(remoteCallsign)
        newFrame.setSourceCallsign(myCallsign)
        newFrame.setControlType(frameType)

        // Set the receive sequence number on frames that require it
        if (listOf(KissFrame.ControlFrame.I_8, KissFrame.ControlFrame.I_8_P,
                KissFrame.ControlFrame.I_128, KissFrame.ControlFrame.I_128_P,
                KissFrame.ControlFrame.S_8_RECEIVE_READY, KissFrame.ControlFrame.S_8_RECEIVE_READY_P,
                KissFrame.ControlFrame.S_8_RECEIVE_NOT_READY, KissFrame.ControlFrame.S_8_RECEIVE_NOT_READY_P,
                KissFrame.ControlFrame.S_8_REJECT, KissFrame.ControlFrame.S_8_REJECT_P,
                KissFrame.ControlFrame.S_8_SELECTIVE_REJECT, KissFrame.ControlFrame.S_8_SELECTIVE_REJECT_P,
                KissFrame.ControlFrame.S_128_RECEIVE_NOT_READY, KissFrame.ControlFrame.S_128_RECEIVE_READY_P,
                KissFrame.ControlFrame.S_128_RECEIVE_READY, KissFrame.ControlFrame.S_128_RECEIVE_READY_P,
                KissFrame.ControlFrame.S_128_REJECT, KissFrame.ControlFrame.S_128_REJECT_P,
                KissFrame.ControlFrame.S_128_SELECTIVE_REJECT, KissFrame.ControlFrame.S_128_SELECTIVE_REJECT_P).contains(frameType)) {
                newFrame.setReceiveSequenceNumber(nextExpectedReceiveSequenceNumber)
        }

        // Set the send sequence number on frames that require it
        if (listOf(KissFrame.ControlFrame.I_8, KissFrame.ControlFrame.I_8_P,
                KissFrame.ControlFrame.I_128, KissFrame.ControlFrame.I_128_P).contains(frameType)) {
            newFrame.setSendSequenceNumber(nextSendSequenceNumber)
            incrementSendSequenceNumber()
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

}