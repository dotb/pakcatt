package pakcatt.network.packet.kiss

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pakcatt.network.packet.kiss.model.KissFrame
import pakcatt.network.packet.kiss.model.KissFrameStandard
import pakcatt.network.packet.kiss.queue.DeliveryQueue
import pakcatt.network.packet.protocol.shared.ProtocolService
import pakcatt.network.packet.tnc.TNC
import pakcatt.util.ByteUtils
import pakcatt.util.StringUtils

@Service
class KissService(val tncConnection: TNC,
                  val protocolServices: List<ProtocolService>,
                  val stringUtils: StringUtils,
                  val byteUtils: ByteUtils) {

    private val logger = LoggerFactory.getLogger(KissService::class.java)
    private var incomingFrame = ByteArray(1024)
    private var incomingFrameIndex = -1

    init {
        tncConnection.setReceiveDataCallback {
            handleNewByte(it)
        }
        tncConnection.connect()
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
        } else {
            // Add the next byte to the current incoming frame.
            incomingFrameIndex++
            incomingFrame[incomingFrameIndex] = newByte
        }
    }

    private fun handleNewFrame(frame: ByteArray) {
        logger.trace("Received bytes:\t {}", stringUtils.byteArrayToHex(frame))
        if (frame.size >= KissFrame.SIZE_MIN) {
            val kissFrame = KissFrameStandard()
            kissFrame.populateFromFrameData(frame)
            logger.trace("Decoded hex:\t\t {}", stringUtils.byteArrayToHex(kissFrame.packetData()))
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