package pakcatt.network.radio.kiss

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.KissFrameStandard
import pakcatt.network.radio.kiss.queue.DeliveryQueue
import pakcatt.network.radio.protocol.shared.ProtocolService
import pakcatt.network.radio.tnc.TNC
import pakcatt.util.ByteUtils
import pakcatt.util.StringUtils
import javax.annotation.PreDestroy

@Service
class KissService(val tncConnections: List<TNC>,
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
        for (tncConnection in tncConnections) {
            tncConnection.setReceiveDataCallback {
                handleNewByte(it, tncConnection)
            }
            tncConnection.connect()
        }
        onStartup()
    }

    @Scheduled(fixedDelay = 500)
    private fun collectAndDeliverFrames() {
        val framesForDelivery = DeliveryQueue()
        for (protocolService in protocolServices) {
            protocolService.queueFramesForDelivery(framesForDelivery)
            if (framesForDelivery.allFrames().isNotEmpty()) {
                logger.trace("Protocol service {} has {} frames for delivery", protocolService, framesForDelivery.allFrames().size)
            }
        }
        for (frame in framesForDelivery.allFrames()) {
            transmitFrame(frame)
        }
    }

    @Scheduled(fixedRate = 1)
    fun serviceTNCOutputBuffers() {
        for (tncConnection in tncConnections) {
            tncConnection.serviceTNCOutputBuffer()
        }
    }

    /**
     * Check the TNC connections and reconnect to any that might have disconnected
     */
    @Scheduled(fixedRate = 60000)
    fun checkTNCConnections() {
        for (tncConnection in tncConnections) {
            if (!tncConnection.isConnected()) {
                logger.info("TNC channel {} is disconnected. Attempting to reconnect.", tncConnection.channelIdentifier)
                tncConnection.connect()
            } else {
                logger.trace("TNC channel {} is connected, sending keepalive.", tncConnection.channelIdentifier)
                tncConnection.sendData(KissFrame.FRAME_END)
            }
        }
    }

    /**
     * This method is called when the PakCatt service is started, and
     * can send a broadcast to announce it's on frequency, if configured.
     */
    private fun onStartup() {
        if (sendStartupShutdownMessage) {
            logger.debug("Sending on frequency broadcast")
            val frame = newBroadcastFrame(startupMessage)
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
            val frame = newBroadcastFrame(shutdownMessage)
            transmitFrame(frame)
        }
    }

    private fun newBroadcastFrame(message: String): KissFrame {
        val frame = KissFrameStandard()
        frame.setControlField(ControlField.U_UNNUMBERED_INFORMATION)
        frame.setSourceCallsign(myCall)
        frame.setDestCallsign("CQ")
        frame.setPayloadMessage(message)
        return frame
    }

    private fun transmitFrame(frame: KissFrame) {
        for (tncConnection in tncConnections) {
            if (tncConnection.channelIdentifier.equals(frame.channelIdentifier)) {
                tncConnection.sendData(frame.packetData())
                tncConnection.sendData(KissFrame.FRAME_END)
                logger.trace("Sent bytes:\t\t {}", stringUtils.byteArrayToHex(frame.packetData()))
                logger.debug("Sent frame:\t\t Chan: {} {}", frame.channelIdentifier, stringUtils.removeEOLChars(frame.toString(), " "))
            } else {
                logger.trace("Not sending frame to TNC: {}, doesn't match frame channel identifier: {} for Frame: {}", tncConnection.channelIdentifier, frame.channelIdentifier, stringUtils.removeEOLChars(frame.toString(), " "))
            }
        }
    }

    private fun handleNewByte(newByte: Byte, tncConnection: TNC) {
        if (KissFrame.FRAME_END == newByte.toInt()) {
            if (incomingFrameIndex >= 0) {
                // Make a copy of the new frame
                val newFrame = incomingFrame.copyOfRange(0, incomingFrameIndex + 1)

                // Reset the current frame memory
                incomingFrameIndex = -1
                incomingFrame = ByteArray(1024)

                // Handle the new frame
                handleNewFrame(newFrame, tncConnection)
            }
        } else if (incomingFrameIndex >= incomingFrame.size) {
            incomingFrameIndex = -1
        } else {
            // Add the next byte to the current incoming frame.
            incomingFrameIndex++
            incomingFrame[incomingFrameIndex] = newByte
        }
    }

    private fun handleNewFrame(frame: ByteArray, tncConnection: TNC) {
        logger.trace("Received bytes:\t {}", stringUtils.byteArrayToHex(frame))
        if (frame.size >= KissFrame.SIZE_MIN) {
            val kissFrame = KissFrameStandard()
            kissFrame.channelIdentifier = tncConnection.channelIdentifier
            kissFrame.populateFromFrameData(frame)
            logger.trace("Recoded bytes:\t {}", stringUtils.byteArrayToHex(kissFrame.packetData()))
            logger.debug("Received frame:\t {}", stringUtils.removeEOLChars(kissFrame.toString(), " "))
            logger.trace("Decoded data:\t {}", kissFrame.payloadDataString())
            handFrameToProtocolServices(kissFrame)
        } else {
            logger.error("KISS frame was too small to decode: ${frame.size} bytes")
        }
    }

    /**
     * Find a protocol service that can handle this incoming Kiss Frame
     */
    private fun handFrameToProtocolServices(frame: KissFrame) {
        for (protocolService in protocolServices) {
            if (protocolService.supportedProtocol(byteUtils.byteToInt(frame.protocolID()), frame.controlField())) {
                protocolService.handleFrame(frame)
            }
        }
    }

}