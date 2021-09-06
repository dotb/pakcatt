package pakcatt.network.radio.kiss

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.KissFrameStandard
import pakcatt.network.radio.kiss.queue.DeliveryQueue
import pakcatt.network.radio.protocol.shared.ProtocolService
import pakcatt.network.radio.tnc.TNC
import pakcatt.util.ByteUtils
import pakcatt.util.StringUtils
import javax.annotation.PreDestroy

@Service
class KissService(val tncConnection: TNC,
                  val protocolServices: List<ProtocolService>,
                  val stringUtils: StringUtils,
                  val byteUtils: ByteUtils,
                  val myCall: String,
                  val sendStartupShutdownMessage: Boolean,
                  val startupMessage: String,
                  val shutdownMessage: String) {

    private val logger = LoggerFactory.getLogger(KissService::class.java)
    private var incomingFrame = ByteArray(1024)
    private var incomingFrameIndex = -1

    init {
        tncConnection.setReceiveDataCallback {
            handleNewByte(it)
        }
        tncConnection.connect()
        onStartup()
    }

    @Scheduled(fixedDelay = 500)
    private fun collectAndDeliverFrames() {
        val framesForDelivery = DeliveryQueue()
        for (protocolService in protocolServices) {
            protocolService.queueFramesForDelivery(framesForDelivery)
        }
        for (frame in framesForDelivery.allFrames()) {
            transmitFrame(frame)
        }
    }

    /**
     * This method is called when the PakCatt service is started, and
     * can send a broadcase to announce it's on frequency, if configured.
     */
    private fun onStartup() {
        if (sendStartupShutdownMessage) {
            logger.debug("Sending on frequency broadcast")
            val frame = KissFrameStandard()
            frame.setSourceCallsign(myCall)
            frame.setDestCallsign("CQ")
            frame.setPayloadMessage(startupMessage)
            transmitFrame(frame)
        }
    }

    /**
     * This method is called when the PakCatt service is shutdown, and
     * can send a QRT broadcast if configured
     */
    @PreDestroy
    private fun onShutdown() {
        if (sendStartupShutdownMessage) {
            logger.debug("Sending QRT broadcast")
            val frame = KissFrameStandard()
            frame.setSourceCallsign(myCall)
            frame.setDestCallsign("CQ")
            frame.setPayloadMessage(shutdownMessage)
            transmitFrame(frame)
        }
    }

    private fun transmitFrame(frame: KissFrame) {
        tncConnection.sendData(frame.packetData())
        tncConnection.sendData(KissFrame.FRAME_END)
        logger.trace("Sent bytes:\t\t {}", stringUtils.byteArrayToHex(frame.packetData()))
        logger.debug("Sent frame:\t\t {}", stringUtils.removeEOLChars(frame.toString(), " "))
    }

    private fun handleNewByte(newByte: Byte) {
        if (KissFrame.FRAME_END == newByte.toInt()) {
            logger.trace("Received boundary of KISS frame after ${incomingFrameIndex + 1} bytes")

            if (incomingFrameIndex >= 0) {
                // Make a copy of the new frame
                val newFrame = incomingFrame.copyOfRange(0, incomingFrameIndex + 1)

                // Reset the current frame memory
                incomingFrameIndex = -1
                incomingFrame = ByteArray(1024)

                // Handle the new frame
                handleNewFrame(newFrame)
            }
        } else if (incomingFrameIndex >= incomingFrame.size) {
            incomingFrameIndex = -1
        } else {
            // Add the next byte to the current incoming frame.
            incomingFrameIndex++
            incomingFrame[incomingFrameIndex] = newByte
        }
    }

    private fun handleNewFrame(frame: ByteArray) {
        logger.debug("Received bytes:\t {}", stringUtils.byteArrayToHex(frame))
        if (frame.size >= KissFrame.SIZE_MIN) {
            val kissFrame = KissFrameStandard()
            kissFrame.populateFromFrameData(frame)
            logger.trace("Recoded bytes:\t {}", stringUtils.byteArrayToHex(kissFrame.packetData()))
            logger.debug("Received frame:\t {}", stringUtils.removeEOLChars(kissFrame.toString(), " "))
            logger.trace("Decoded data:\t {}", kissFrame.payloadDataString())
            handFrameToProtocolServices(kissFrame)
        } else {
            logger.error("KISS frame was too small to decode: ${frame.size} bytes")
        }
    }

    private fun handFrameToProtocolServices(frame: KissFrame) {
        for (protocolService in protocolServices) {
            if (protocolService.supportedProtocol(byteUtils.byteToInt(frame.protocolID()), frame.controlField())) {
                protocolService.handleFrame(frame)
            }
        }
    }

}