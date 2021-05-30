package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissFrameExtended
import pakcatt.network.packet.kiss.KissFrameStandard
import pakcatt.network.packet.kiss.KissService

@Service
class LinkService(var kissService: KissService,
                  var myCall: String) {

    private val logger = LoggerFactory.getLogger(LinkService::class.java)
    private var rxCounter = 0

    init {
        kissService.setReceiveFrameCallback {
            handleReceivedFrame(it)
        }

        /*
        val testFrame = KissFrame()
        testFrame.setDestCallsign("VK3LIT-2")
        testFrame.setSourceCallsign("VK3LIT-1")
        testFrame.setControlType(KissFrame.ControlFrame.U_FRAME_UNNUMBERED_INFORMATION)
//        testFrame.setReceiveSequenceNumber(4)
        testFrame.setPayloadMessage("A")
        logger.debug("Test frame hex ${StringUtils.byteArrayToHex(testFrame.packetData())}")
        logger.debug("Test frame ${testFrame.toString()}")
        transmitQueue.push(testFrame)
         */
    }


    private fun handleReceivedFrame(frame: KissFrame) {
        if (isMyFrame(frame)) {
            logger.debug("Frame addressed to me: ${frame.toString()}")
            handleMyFrame(frame)
        } else {
            logger.debug("Frame not addressed to me: ${frame.toString()}")
        }
    }

    private fun handleMyFrame(frame: KissFrame) {
        when (frame.controlFrame()) {
            KissFrame.ControlFrame.I_8_P -> handApplicationFrame(frame)
            KissFrame.ControlFrame.U_SET_ASYNC_BALANCED_MODE_P -> sendUnnumberedAcknowlege(frame)
//            KissFrame.ControlFrame.S_8_RECEIVE_READY_P -> sendUnnumberedAcknowlege(frame)
            KissFrame.ControlFrame.S_8_RECEIVE_READY_P -> sendRecieveReadyUpdate(frame)
            KissFrame.ControlFrame.U_DISCONNECT_P -> sendUnnumberedAcknowlege(frame)
            else -> ignoreFrame(frame)
        }
    }

    private fun isMyFrame(frame: KissFrame): Boolean {
        return frame.destCallsign().equals(myCall, ignoreCase = true)
    }

    /* Application interface */
    private fun handApplicationFrame(frame: KissFrame) {
        logger.debug("Application frame: ${frame.toString()}")
        rxCounter++
        sendReceiveReady(frame)
    }

    /* Link layer responses */
    private fun sendDisconnectMode(incomingFrame: KissFrame) {
        val frame = newResponseFrame(incomingFrame.sourceCallsign(), KissFrame.ControlFrame.U_DISCONNECT, false)
        kissService.queueFrameForTransmission(frame)
    }

    private fun sendUnnumberedAcknowlege(incomingFrame: KissFrame) {
        val frame = newResponseFrame(incomingFrame.sourceCallsign(), KissFrame.ControlFrame.U_UNNUMBERED_ACKNOWLEDGE_P, false)
        kissService.queueFrameForTransmission(frame)
    }

    private fun sendRecieveReadyUpdate(incomingFrame: KissFrame) {
        val frame = newResponseFrame(incomingFrame.sourceCallsign(), KissFrame.ControlFrame.S_8_RECEIVE_READY_P, false)
        frame.setReceiveSequenceNumber(rxCounter)
        kissService.queueFrameForTransmission(frame)
    }

    private fun sendReceiveReady(incomingFrame: KissFrame) {
        val frame = newResponseFrame(incomingFrame.sourceCallsign(), KissFrame.ControlFrame.S_8_RECEIVE_READY, false)
        frame.setReceiveSequenceNumber(rxCounter)
        kissService.queueFrameForTransmission(frame)
    }

    private fun ignoreFrame(incomingFrame: KissFrame) {
        logger.info("Frame ignored: ${incomingFrame.toString()}")
    }

    /* Factory methods */
    private fun newResponseFrame(destCallsign: String, frameType: KissFrame.ControlFrame, extended: Boolean): KissFrame {
        val newFrame = when (extended) {
            false -> KissFrameStandard()
            true -> KissFrameExtended()
        }
        newFrame.setDestCallsign(destCallsign)
        newFrame.setSourceCallsign(myCall)
        newFrame.setControlType(frameType)
        return newFrame
    }

}