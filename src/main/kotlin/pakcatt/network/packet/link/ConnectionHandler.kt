package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import pakcatt.network.packet.link.model.ConnectionResponse
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissFrameExtended
import pakcatt.network.packet.kiss.KissFrameStandard
import pakcatt.network.packet.link.model.LinkRequest

class ConnectionHandler(val remoteCallsign: String,
                        val myCallsign: String,
                        val linkInterface: LinkInterface) {

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
    private var lastSentSequenceNumberAcknowledged = 0


    fun handleRequestToSendMessageFromApp(request: LinkRequest) {
        sendMessage(request.message, false)
    }

    fun handleIncomingFrame(incomingFrame: KissFrame) {
        when (incomingFrame.controlFrame()) {
            KissFrame.ControlFrame.U_SET_ASYNC_BALANCED_MODE_P -> handleConnectionRequest(incomingFrame) // Connection request
            KissFrame.ControlFrame.INFORMATION_8_P -> handleNumberedInformationFrame(incomingFrame, true) // Application info frame
            KissFrame.ControlFrame.INFORMATION_8 -> handleNumberedInformationFrame(incomingFrame, false) // Application info frame
            KissFrame.ControlFrame.S_8_RECEIVE_READY_P -> handleIncomingAcknowledgement(incomingFrame)
            KissFrame.ControlFrame.S_8_RECEIVE_READY -> handleIncomingAcknowledgement(incomingFrame)
            KissFrame.ControlFrame.S_8_REJECT -> realignReceiveSequenceNumber(incomingFrame)
            KissFrame.ControlFrame.S_8_REJECT_P -> realignReceiveSequenceNumber(incomingFrame)
            KissFrame.ControlFrame.U_REJECT -> realignReceiveSequenceNumber(incomingFrame)
            KissFrame.ControlFrame.U_DISCONNECT_P -> handleDisconnectRequest() // Disconnect request
            else -> ignoreFrame(incomingFrame)
        }
    }

    /* Application interface */
    private fun handleNumberedInformationFrame(incomingFrame: KissFrame, pACKRequired: Boolean) {
        if (incomingFrame.sendSequenceNumber() == nextExpectedReceiveSequenceNumber) {
            incrementReceiveSequenceNumber()

            // Share the payload with any listening applications to process
            val  appResponse = linkInterface.getResponseForReceivedMessage(LinkRequest(incomingFrame.sourceCallsign(), incomingFrame.destCallsign(), incomingFrame.payloadDataString()))
            when (appResponse.responseType) {
                InteractionResponse.InteractionResponseType.SEND_TEXT -> acknowledgeAndSendMessage(appResponse.message, pACKRequired)
                InteractionResponse.InteractionResponseType.ACK_ONLY -> acknowledgeBySendingReadyReceive(pACKRequired)
                InteractionResponse.InteractionResponseType.IGNORE -> logger.trace("Apps ignored frame: ${incomingFrame.toString()}")
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
            ConnectionResponse.ConnectionResponseType.CONNECT -> acceptIncomingConnection()
            ConnectionResponse.ConnectionResponseType.CONNECT_WITH_MESSAGE -> acceptIncomingConnectionWithMessage(appResponse.message)
            ConnectionResponse.ConnectionResponseType.IGNORE -> logger.trace("Ignored connection request from: $remoteCallsign to :$myCallsign")
        }
    }

    private fun acceptIncomingConnection() {
        logger.info("Accepting connection from remote party: $remoteCallsign local party: $myCallsign")
        // Reset sequence state
        controlMode = ControlMode.MODULO_8
        nextExpectedReceiveSequenceNumber = 0
        nextSendSequenceNumber = 0
        lastSentSequenceNumberAcknowledged = 0
        val frame = newResponseFrame(KissFrame.ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        linkInterface.queueFrameForDelivery(frame)
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
        val expectedReceiveSequenceNumber = nextSendSequenceNumber
        if (incomingFrame.receiveSequenceNumber() != expectedReceiveSequenceNumber) {
            logger.error("Remote party $remoteCallsign may have missed our sent message numbered ${expectedReceiveSequenceNumber - 1}")
            realignReceiveSequenceNumber(incomingFrame)
        }

        // Update our last acknowledged sequence number. If it's already updated then the remote party may be asking to re-sync with an RR_P
        if (lastSentSequenceNumberAcknowledged == incomingFrame.receiveSequenceNumber()) {
            logger.debug("Our last send sequence number has already been ACKed, sending an RR_P to re-sync.")
            acknowledgeBySendingReadyReceive(true)
        } else {
            lastSentSequenceNumberAcknowledged = incomingFrame.receiveSequenceNumber()
        }
    }

    private fun acknowledgeBySendingReadyReceive(pACKRequired: Boolean) {
        val frame = when (pACKRequired) {
            true -> newResponseFrame(KissFrame.ControlFrame.S_8_RECEIVE_READY_P, false)
            false -> newResponseFrame(KissFrame.ControlFrame.S_8_RECEIVE_READY, false)
        }
        linkInterface.queueFrameForDelivery(frame)
    }

    private fun realignSendSequenceNumber(incomingFrame: KissFrame) {
        logger.error("Re-aligning receive sequence number: ${incomingFrame.toString()}")
        nextExpectedReceiveSequenceNumber = incomingFrame.sendSequenceNumber()
        acknowledgeBySendingReadyReceive(true)
    }

    private fun realignReceiveSequenceNumber(incomingFrame: KissFrame) {
        logger.error("Re-aligning send sequence number: ${incomingFrame.toString()}")
        nextSendSequenceNumber = incomingFrame.receiveSequenceNumber()
        acknowledgeBySendingReadyReceive(true)
    }

    private fun handleDisconnectRequest() {
        logger.trace("Disconnecting from $remoteCallsign")
        val frame = newResponseFrame(KissFrame.ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        linkInterface.queueFrameForDelivery(frame)
        linkInterface.closeConnection(remoteCallsign, myCallsign)
    }

    private fun ignoreFrame(incomingFrame: KissFrame) {
        logger.error("No handler for frame. Ignored. ${incomingFrame.toString()}")
        linkInterface.closeConnection(remoteCallsign, myCallsign)
    }

    /* Application Interface methods */
    private fun sendMessage(message: String, pACKRequired: Boolean) {
        val messageWithEOL = "$message\n\r" // And EOL characters
        val frame = when (pACKRequired) {
            true -> newResponseFrame(KissFrame.ControlFrame.INFORMATION_8_P, false)
            false -> newResponseFrame(KissFrame.ControlFrame.INFORMATION_8, false)
        }
        frame.setPayloadMessage(messageWithEOL)
        linkInterface.queueFrameForDelivery(frame)
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
        if (listOf(KissFrame.ControlFrame.INFORMATION_8, KissFrame.ControlFrame.INFORMATION_8_P,
                KissFrame.ControlFrame.INFORMATION_128, KissFrame.ControlFrame.INFORMATION_128_P,
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
        if (listOf(KissFrame.ControlFrame.INFORMATION_8, KissFrame.ControlFrame.INFORMATION_8_P,
                KissFrame.ControlFrame.INFORMATION_128, KissFrame.ControlFrame.INFORMATION_128_P).contains(frameType)) {
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