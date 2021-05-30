package pakcatt.network.packet.kiss

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pakcatt.network.packet.tnc.TNC
import pakcatt.util.StringUtils
import java.util.*

@Service
class KissService(val tncConnection: TNC, val stringUtils: StringUtils) {

    private val logger = LoggerFactory.getLogger(KissService::class.java)
    private lateinit var receiveFrameCallback:(receivedFrame: KissFrame) -> Unit?
    private var incomingFrame = ByteArray(1024)
    private var incomingFrameIndex = -1
    private var transmitQueue = LinkedList<KissFrame>()

    init {
        tncConnection.setReceiveDataCallback {
            handleNewByte(it)
        }
        tncConnection.connect()
    }

    /**
     * Set the callback handler for incoming decoded KISS frames
     */
    fun setReceiveFrameCallback(newCallback: (receivedFrame: KissFrame) -> Unit) {
        receiveFrameCallback = newCallback
    }

    fun queueFrameForTransmission(frame: KissFrame) {
        transmitQueue.add(frame)
    }

    @Scheduled(fixedRate = 1000)
    private fun serviceTransmitQueue() {
        while (!transmitQueue.isEmpty()) {
            val nextFrame = transmitQueue.pollFirst()
            logger.debug("Sending frame: ${nextFrame.toString()}")
            tncConnection.sendData(nextFrame.packetData())
            tncConnection.sendData(KissFrame.FRAME_END)
            logger.debug("Sent frame:\t\t ${stringUtils.byteArrayToHex(nextFrame.packetData())}")
        }
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
        logger.debug("Received frame:\t ${stringUtils.byteArrayToHex(frame)}")
        val myReceiveFrameCallback = receiveFrameCallback
        if (frame.size >= KissFrame.SIZE_MIN && null != myReceiveFrameCallback) {
            val kissFrame = KissFrame.parseRawKISSFrame(frame)
            logger.debug("Decoded hex:\t\t ${stringUtils.byteArrayToHex(kissFrame.packetData())}")
            logger.debug("Decoded meta:\t ${kissFrame.toString()}")
            logger.debug("Decoded data:\t ${kissFrame.payloadDataString()}")
            myReceiveFrameCallback(kissFrame)
        } else {
            logger.error("KISS frame was too small to decode: ${frame.size} bytes")
        }
    }

}