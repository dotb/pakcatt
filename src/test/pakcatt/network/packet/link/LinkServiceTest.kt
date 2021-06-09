package pakcatt.network.packet.link

import junit.framework.TestCase
import org.awaitility.Awaitility.await
import org.awaitility.Duration
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import pakcatt.application.TestApp
import pakcatt.network.packet.kiss.ControlFrame
import pakcatt.network.packet.kiss.KissFrame
import pakcatt.network.packet.kiss.KissFrameStandard
import pakcatt.network.packet.tnc.TNC
import pakcatt.network.packet.tnc.TNCMocked
import pakcatt.util.ByteUtils
import pakcatt.util.StringUtils


@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
class LinkServiceTest: TestCase() {

    val byteUtils = ByteUtils()
    val stringUtils = StringUtils()

    @Autowired
    lateinit var tnc: TNC

    @Autowired
    lateinit var linkService: LinkService

    @Test
    fun testStandardConversationHandshake() {
        val mockedTNC = tnc as TNCMocked

        /* Accept an incoming conversation, respond with an Unnumbered ACK */
        // From: VK3LIT-2 to: VK3LIT-1 control: 3f  controlType: U_SET_ASYNC_BALANCED_MODE_P pollFinalBit: 1 protocolID: 80
        sendFrameFromBytesAndWaitResponse(mockedTNC, byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x62, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x3f))
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1
        assertResponse(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0), mockedTNC)


        /* Receive an incoming message, respond with an Ready Receive ACK*/
        // Send nop
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.INFORMATION_8, 0, 0, "nop")
        // Receive ACK only
        assertResponse(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x21, 0xc0), mockedTNC)

        /* Receive an Receive Ready P message and transfer rx variable state back to the remote TNC */
        // Send Hello!
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.INFORMATION_8, 1, 0, "Hello!")
        // Receive two frames - 1 INFORMATION_8 and 2 S_8_RECEIVE_READY_P
        val responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        assertEquals(ControlFrame.INFORMATION_8, responseFrames[0].controlFrame())
        assertEquals("Hi, there! *wave*", responseFrames[0].payloadDataString())
        assertEquals(ControlFrame.S_8_RECEIVE_READY_P, responseFrames[1].controlFrame())

        /* Receive a disconnect, and respond with an Unnumbered ACK */
        // From: VK3LIT-2 to: VK3LIT-1 control: 53  controlType: U_DISCONNECT_P pollFinalBit: 1 protocolID: 80 Receive Seq: 2 Send Seq: 1
        sendFrameFromBytesAndWaitResponse(mockedTNC, byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe2, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x53))
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1 protocolID: f0 Receive Seq: 3 Send Seq: 1
        assertResponse(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0), mockedTNC)
    }

    @Test
    fun testSequenceNumbersAndRollover() {
        val mockedTNC = tnc as TNCMocked
        // Establish a link
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.U_SET_ASYNC_BALANCED_MODE_P, 0, 0)
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1
        assertResponse(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0), mockedTNC)

        // Send multiple message exchanges to ensure sequence numbers increment and roll over properly after 7 (a 3 bit value)
        var rxSequenceNumber = 0
        for (sendSequenceNumber in 0..6) {
            sendFrameAndWaitResponse(mockedTNC, ControlFrame.INFORMATION_8, sendSequenceNumber, rxSequenceNumber, "hello")
            val responseFrame = KissFrame.parseRawKISSFrame(mockedTNC.sentDataBuffer())
            rxSequenceNumber = sendSequenceNumber + 1
            assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", sendSequenceNumber + 1, responseFrame.receiveSequenceNumber())
            assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", sendSequenceNumber, responseFrame.sendSequenceNumber())
        }

        // The 7th exchange should roll-over the received sequence number
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.INFORMATION_8, 7, rxSequenceNumber, "hello")
        var responseFrame = KissFrame.parseRawKISSFrame(mockedTNC.sentDataBuffer())
        assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", 0, responseFrame.receiveSequenceNumber())
        assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", 7, responseFrame.sendSequenceNumber())

        // The 8th exchange should roll-over the sent sequence number
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.INFORMATION_8, 0, rxSequenceNumber, "hello")
        responseFrame = KissFrame.parseRawKISSFrame(mockedTNC.sentDataBuffer())
        assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", 1, responseFrame.receiveSequenceNumber())
        assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", 0, responseFrame.sendSequenceNumber())

    }

    @Test
    fun testShortResponse() {
        val mockedTNC = tnc as TNCMocked

        // Establish a link
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.U_SET_ASYNC_BALANCED_MODE_P, 0, 0)

        // Send request
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.INFORMATION_8, 0, 0, "ping")
        val parsedFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        val constructedPayload = constructPayloadFromFrames(parsedFrames)
        assertEquals("pong\n\r", constructedPayload)
    }

    @Test
    fun testChunkingOfLongResponse() {
        val mockedTNC = tnc as TNCMocked

        var receivedFrames = ArrayList<KissFrame>()

        // Establish a link
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.U_SET_ASYNC_BALANCED_MODE_P, 0, 0)

        // Send request for a large response
        sendFrameAndWaitResponse(mockedTNC, ControlFrame.INFORMATION_8, 0, 0, "longtest")
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        var lastReceivedFrame = receivedFrames.last()
        while (lastReceivedFrame.controlFrame() != ControlFrame.S_8_RECEIVE_READY_P) {
            val nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
            sendFrameAndWaitResponse(mockedTNC, ControlFrame.S_8_RECEIVE_READY, 0, nextExpectedSendSequenceNumber)
            receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
            lastReceivedFrame = receivedFrames.last()
        }
        val constructedPayload = constructPayloadFromFrames(receivedFrames)
        val expectedResponseString = "${TestApp.longResponseString}\n\r"
        assertEquals(expectedResponseString, constructedPayload)
    }

    private fun sendFrameAndWaitResponse(mockedTNC: TNCMocked, controlType: ControlFrame, sendSequenceNumber: Int, rxSequenceNumber: Int, payload: String? = null) {
        val requestFrame = KissFrameStandard()
        requestFrame.setDestCallsign("VK3LIT-1")
        requestFrame.setSourceCallsign("VK3LIT-2")
        requestFrame.setControlType(controlType)
        requestFrame.setSendSequenceNumber(sendSequenceNumber)
        requestFrame.setReceiveSequenceNumber(rxSequenceNumber)
        if (null != payload) {
            requestFrame.setPayloadMessage(payload)
        }
        sendFrameFromBytesAndWaitResponse(mockedTNC, requestFrame.packetData())
    }

    private fun sendFrameFromBytesAndWaitResponse(mockedTNC: TNCMocked, frameData: ByteArray) {
        // Send conversation request
        mockedTNC.clearDataBuffer()
        mockedTNC.receiveDataCallback(byteUtils.intToByte(0xC0))
        for (byte in frameData) {
            mockedTNC.receiveDataCallback(byte)
        }
        mockedTNC.receiveDataCallback(byteUtils.intToByte(0xC0))
        waitForResponse(mockedTNC)
    }

    private fun byteArrayFromInts(vararg elements: Int): ByteArray {
        val byteArray = ByteArray(elements.size)
        for ((index, intOctet) in elements.withIndex()) {
            val byte = byteUtils.intToByte(intOctet)
            byteArray[index] = byte
        }
        return byteArray
    }

    private fun constructPayloadFromFrames(frames: List<KissFrame>): String {
        val singleString = StringBuilder()
        for (frame in frames) {
            singleString.append(frame.payloadDataString())
        }
        return singleString.toString()
    }

    private fun parseFramesFromResponse(responseBuffer: ByteArray): List<KissFrame> {
        var responseFrameList = ArrayList<KissFrame>()
        var frameStart = 0
        var frameEnd = 0
        for (byte in responseBuffer) {
            when (byte) {
                // We've come to the end of the frame
                0xC0.toByte() -> {
                    val frameSize = frameEnd - frameStart
                    val frameBuffer = ByteArray(frameSize)
                    responseBuffer.copyInto(frameBuffer, 0, frameStart, frameEnd)
                    val kissFrame = KissFrame.parseRawKISSFrame(frameBuffer)
                    responseFrameList.add(kissFrame)
                    val payloadString = kissFrame.payloadDataString()
                    frameEnd++
                    frameStart = frameEnd
                }
                // Increase the index so that these bytes are included in the next frame
                else -> frameEnd++
            }
        }
        return responseFrameList
    }

    private fun waitForResponse(mockedTNC: TNCMocked) {
        // Wait for the response
        await().atMost(Duration.TEN_SECONDS).until {
            mockedTNC.sentDataBuffer().size >= 10
        }
    }

    private fun assertResponse(expectedResponse: ByteArray, mockedTNC: TNCMocked) {
        val response = mockedTNC.sentDataBuffer()
        assertEquals(stringUtils.byteArrayToHex(expectedResponse), stringUtils.byteArrayToHex(response))
    }

}