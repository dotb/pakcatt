package pakcatt.network.radio.protocol

import junit.framework.TestCase
import org.awaitility.Awaitility
import org.awaitility.Duration
import org.slf4j.LoggerFactory
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.KissFrameStandard
import pakcatt.network.radio.protocol.packet.PacketService
import pakcatt.network.radio.tnc.TNCMocked
import pakcatt.util.ByteUtils
import pakcatt.util.StringUtils
import java.util.concurrent.TimeUnit

abstract class ProtocolTest: TestCase() {

    private val logger = LoggerFactory.getLogger(ProtocolTest::class.java)
    protected val byteUtils = ByteUtils()
    protected val stringUtils = StringUtils()

    protected fun sendFrameAndWaitResponse(mockedTNC: TNCMocked, controlType: ControlField, sendSequenceNumber: Int, rxSequenceNumber: Int, payload: String? = null, expectedFramesInResponse: Int = 1) {
        sendFrame(mockedTNC, controlType, sendSequenceNumber, rxSequenceNumber, payload)
        waitForResponse(mockedTNC, expectedFramesInResponse)
    }

    protected fun sendFrame(mockedTNC: TNCMocked, controlType: ControlField, sendSequenceNumber: Int, rxSequenceNumber: Int, payload: String? = null) {
        val requestFrame = KissFrameStandard()
        requestFrame.setDestCallsign("VK3LIT-1")
        requestFrame.setSourceCallsign("VK3LIT-2")
        requestFrame.setControlField(controlType, rxSequenceNumber, sendSequenceNumber)
        if (null != payload) {
            requestFrame.setPayloadMessage(payload)
        }
        sendFrameFromBytes(mockedTNC, requestFrame.packetData())
    }

    protected fun sendFrameFromBytesAndWaitResponse(mockedTNC: TNCMocked, frameData: ByteArray, expectedFramesInResponse: Int = 1) {
        sendFrameFromBytes(mockedTNC, frameData)
        waitForResponse(mockedTNC, expectedFramesInResponse)
    }

    protected fun sendFrameFromBytes(mockedTNC: TNCMocked, frameData: ByteArray) {
        // Send conversation request
        mockedTNC.clearDataBuffer()
        mockedTNC.receiveDataCallback(byteUtils.intToByte(0xC0))
        for (byte in frameData) {
            mockedTNC.receiveDataCallback(byte)
        }
        mockedTNC.receiveDataCallback(byteUtils.intToByte(0xC0))
    }

    protected fun constructPayloadFromFrames(frames: List<KissFrame>): String {
        val singleString = StringBuilder()
        for (frame in frames) {
            singleString.append(frame.payloadDataString())
        }
        return singleString.toString()
    }

    protected fun parseFramesFromResponse(responseBuffer: ByteArray): List<KissFrame> {
        var responseFrameList = ArrayList<KissFrame>()
        var frameStart = 0
        var frameEnd = 0
        for (byte in responseBuffer) {
            when (byte) {
                0xC0.toByte() -> {
                    // We've come to the end of the frame
                    val frameSize = frameEnd - frameStart
                    val frameBuffer = ByteArray(frameSize)
                    responseBuffer.copyInto(frameBuffer, 0, frameStart, frameEnd)
                    logger.trace("parsed a frame buffer: {}", frameBuffer)
                    if (frameBuffer.isNotEmpty()) {
                        /* Only populate a frame if the buffer is populated. The TNC will send empty frames
                        * as keep-alive packets, messing up long running tests */
                        val kissFrame = KissFrameStandard()
                        kissFrame.populateFromFrameData(frameBuffer)
                        responseFrameList.add(kissFrame)
                    }
                    frameEnd++
                    frameStart = frameEnd
                }
                // Increase the index so that these bytes are included in the next frame
                else -> frameEnd++
            }
        }
        return responseFrameList
    }

    protected fun waitForResponse(mockedTNC: TNCMocked, expectedFramesInResponse: Int) {
        // Wait for the response
        Awaitility.await().atMost(Duration.TEN_SECONDS).until {
            mockedTNC.numberOfFramesInSendDataBuffer() >= expectedFramesInResponse
        }
    }

    protected fun assertResponse(expectedResponse: ByteArray, mockedTNC: TNCMocked) {
        val response = mockedTNC.sentDataBuffer()
        assertEquals(stringUtils.byteArrayToHex(expectedResponse), stringUtils.byteArrayToHex(response))
    }

}