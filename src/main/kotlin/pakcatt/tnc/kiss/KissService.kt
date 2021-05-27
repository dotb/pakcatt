package pakcatt.tnc.kiss

import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import pakcatt.tnc.TNC
import pakcatt.util.StringUtils
import java.util.*

@Service
class KissService(val tncConnection: TNC) {

    private val logger = LoggerFactory.getLogger(KissService::class.java)
    private var incomingFrame = ByteArray(1024)
    private var incomingFrameIndex = -1
    private var transmitQueue = LinkedList<KissFrame>()

    init {
        tncConnection.setReceiveDataCallback {
            handleNewByte(it)
        }

        tncConnection.connect()
/*
        val ctrlC = 0x03
        val ctrlCCommand = ByteArray(1)
        ctrlCCommand[0] = ctrlC.toByte()
        tncConnection.sendData(ctrlCCommand)
        tncConnection.sendData(ctrlCCommand)
        tncConnection.sendData(ctrlCCommand)
        tncConnection.sendData("echo off\r\n".toByteArray())
        tncConnection.sendData("connect VK2VRO\r\n".toByteArray())
        Thread.sleep(400)
        tncConnection.sendData(ctrlCCommand)
        tncConnection.sendData("KISS $01\r\n".toByteArray())
*/

        val testFrame = KissFrame()
        testFrame.setDestCallsign("VK3LIT-2")
        testFrame.setSourceCallsign("VK3LIT-1")
        testFrame.setReceiveSequenceNumber(4)
        logger.debug("Test frame ${testFrame.toString()}")
        transmitQueue.push(testFrame)
    }

    @Scheduled(fixedRate = 2000)
    private fun serviceTransmitQueue() {
        if (!transmitQueue.isEmpty()) {
            val nextFrame = transmitQueue.pop()
            logger.debug("Sending frame from: ${nextFrame.sourceCallsign()} to: ${nextFrame.destCallsign()}")
            tncConnection.sendData(KissFrame.FRAME_END)
            tncConnection.sendData(nextFrame.packetData())
            tncConnection.sendData(KissFrame.FRAME_END)
        }
    }

    private fun handleNewByte(newByte: Byte) {
        if (KissFrame.FRAME_END == newByte.toInt()) {
            logger.trace("Received boundary of KISS frame after ${incomingFrameIndex + 1} bytes")

            if (incomingFrameIndex >= 0) {
                // Make a copy of the new frame
                val newFrame = incomingFrame.copyOfRange(0, incomingFrameIndex)

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
        logger.trace("Received AX.25 frame: ${StringUtils.byteArrayToHex(frame)}")
        if (frame.size >= KissFrame.SIZE_MIN) {
            val kissFrame = createKissFrame(frame)
            logger.debug("Frame ${kissFrame.toString()}")
            logger.trace("Frame data hex: ${StringUtils.byteArrayToHex(kissFrame.payloadData())}")
            logger.debug("Frame data string: ${kissFrame.payloadDataString()}")
        } else {
            logger.error("KISS frame was too small to decode: ${frame.size} bytes")
        }
    }

    fun createKissFrame(frame: ByteArray): KissFrame {
        // Mandatory fields
        val portAndCommand = frame[0]
        val destCallsign = frame.copyOfRange(1, 7)
        val destSSID = frame[7]
        val sourceCallsign = frame.copyOfRange(8, 14)
        val sourceSSID = frame[14]

        // Optional fields
        var controlField = Byte.MIN_VALUE
        var protocolID = Byte.MIN_VALUE
        var payloadData = ByteArray(0)
        if (frame.size >= 16) {
            controlField = frame[15]
        }
        if (frame.size >= 17) {
            protocolID = frame[16]
        }
        if (frame.size >= 18) {
            payloadData = frame.copyOfRange(17, frame.size)
        }
        val kissFrame = KissFrame()
        kissFrame.parseRawKISSFrame(portAndCommand,
                destCallsign,
                destSSID,
                sourceCallsign,
                sourceSSID,
                controlField,
                protocolID,
                payloadData)
        return kissFrame
    }

}