package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissService

@Service
class LinkService(var kissService: KissService,
                  var myCall: String) {

    private val logger = LoggerFactory.getLogger(LinkService::class.java)

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
            KissFrame.ControlFrame.I_FRAME -> handApplicationFrame(frame)
            KissFrame.ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED -> sendDisconnectMode(frame) // Not yet supported
            else -> ignoreFrame(frame)
        }
    }

    private fun isMyFrame(frame: KissFrame): Boolean {
        return frame.destCallsign().equals(myCall, ignoreCase = true)
    }

    /* Application interface */
    private fun handApplicationFrame(frame: KissFrame) {
        logger.debug("Application frame: ${frame.toString()}")
    }

    /* Link layer responses */
    private fun sendDisconnectMode(frame: KissFrame) {
        val frame = newResponseFrame(frame.sourceCallsign(), KissFrame.ControlFrame.U_FRAME_DISCONNECT_MODE)
        kissService.queueFrameForTransmission(frame)
    }

    private fun ignoreFrame(frame: KissFrame) {
        logger.error("Frame ignored: ${frame.toString()}")
    }

    /* Factory methods */
    private fun newResponseFrame(destCallsign: String, frameType: KissFrame.ControlFrame): KissFrame {
        val newFrame = KissFrame()
        newFrame.setDestCallsign(destCallsign)
        newFrame.setSourceCallsign(myCall)
        newFrame.setControlType(frameType)
        return newFrame
    }

}