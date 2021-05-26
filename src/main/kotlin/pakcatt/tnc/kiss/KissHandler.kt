package pakcatt.tnc.kiss

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.tnc.TNC
import pakcatt.util.Utils

@Service
class KissHandler(val tncConnection: TNC) {

    private val logger = LoggerFactory.getLogger(KissHandler::class.java)
    private var incomingFrame = ByteArray(1024)
    private var incomingFrameIndex = -1

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
    }

    private fun handleNewByte(newByte: Byte) {
        if (KissFrame.FRAME_END == newByte.toInt()) {
            logger.debug("Received boundary of KISS frame after ${incomingFrameIndex + 1} bytes")

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
        logger.debug("Received AX.25 frame: ${Utils.byteArrayToHex(frame)}")
        if (frame.size >= KissFrame.SIZE_MIN) {
            val kissFrame = createKissFrame(frame)
            logger.debug("Frame from: ${kissFrame.sourceCallsign()} to: ${kissFrame.destCallsign()} control: ${Utils.byteToHex(kissFrame.controlField())} protocolID: ${Utils.byteToHex(kissFrame.protocolID())} controlType: ${kissFrame.controlTypeString()}")
            logger.debug("Frame data hex: ${Utils.byteArrayToHex(kissFrame.payloadData())}")
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
        val kissFrame = KissFrame(portAndCommand,
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