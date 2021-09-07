package pakcatt.network.radio.protocol.packet

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import pakcatt.application.TestApp
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.KissFrameStandard
import pakcatt.network.radio.protocol.ProtocolTest
import pakcatt.network.radio.tnc.TNC
import pakcatt.network.radio.tnc.TNCMocked
import pakcatt.util.StringUtils


@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
class NoLayerThreeServiceTest: ProtocolTest() {

    @Autowired
    lateinit var tnc: TNC

    @Test
    fun testStandardConversationHandshake() {
        val mockedTNC = tnc as TNCMocked

        /* Accept an incoming conversation, respond with an Unnumbered ACK */
        // From: VK3LIT-2 to: VK3LIT-1 control: 3f  controlType: U_SET_ASYNC_BALANCED_MODE_P pollFinalBit: 1 protocolID: 80
        sendFrameFromBytesAndWaitResponse(mockedTNC, byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x62, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x3f))
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1
        assertResponse(byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0), mockedTNC)


        /* Receive an incoming message, respond with an Ready Receive ACK*/
        // Send nop
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 0, 0, "nop")
        // Receive ACK only
        assertResponse(byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x21, 0xc0), mockedTNC)

        /* Receive an Receive Ready P message and transfer rx variable state back to the remote TNC */
        // Send Hello!
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 1, 0, "Hello!")
        // Receive two frames - 1 INFORMATION_8 and 2 S_8_RECEIVE_READY_P
        val responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        assertEquals(ControlField.INFORMATION_8, responseFrames[0].controlField())
        assertEquals("Hi, there! *wave*${stringUtils.EOL}", responseFrames[0].payloadDataString())
        assertEquals(ControlField.S_8_RECEIVE_READY_P, responseFrames[1].controlField())

        /* Receive a disconnect, and respond with an Unnumbered ACK */
        // From: VK3LIT-2 to: VK3LIT-1 control: 53  controlType: U_DISCONNECT_P pollFinalBit: 1 protocolID: 80 Receive Seq: 2 Send Seq: 1
        sendFrameFromBytesAndWaitResponse(mockedTNC, byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe2, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x53))
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1 protocolID: f0 Receive Seq: 3 Send Seq: 1
        assertResponse(byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0), mockedTNC)
    }

    @Test
    fun testSequenceNumbersAndRollover() {
        val mockedTNC = tnc as TNCMocked
        // Establish a link
        sendFrameAndWaitResponse(mockedTNC, ControlField.U_SET_ASYNC_BALANCED_MODE_P, 0, 0)
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1
        assertResponse(byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0), mockedTNC)

        // Send multiple message exchanges to ensure sequence numbers increment and roll over properly after 7 (a 3 bit value)
        var rxSequenceNumber = 0
        for (sendSequenceNumber in 0..6) {
            sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, sendSequenceNumber, rxSequenceNumber, "hello")
            var responseFrame = KissFrameStandard()
            responseFrame.populateFromFrameData(mockedTNC.sentDataBuffer())
            rxSequenceNumber = sendSequenceNumber + 1
            assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", sendSequenceNumber + 1, responseFrame.receiveSequenceNumber())
            assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", sendSequenceNumber, responseFrame.sendSequenceNumber())
        }

        // The 7th exchange should roll-over the received sequence number
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 7, rxSequenceNumber, "hello")
        var responseFrame = KissFrameStandard()
        responseFrame.populateFromFrameData(mockedTNC.sentDataBuffer())
        assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", 0, responseFrame.receiveSequenceNumber())
        assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", 7, responseFrame.sendSequenceNumber())

        // The 8th exchange should roll-over the sent sequence number
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 0, rxSequenceNumber, "hello")
        responseFrame = KissFrameStandard()
        responseFrame.populateFromFrameData(mockedTNC.sentDataBuffer())
        assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", 1, responseFrame.receiveSequenceNumber())
        assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", 0, responseFrame.sendSequenceNumber())

    }

    @Test
    fun testShortResponse() {
        val mockedTNC = tnc as TNCMocked

        // Establish a link
        sendFrameAndWaitResponse(mockedTNC, ControlField.U_SET_ASYNC_BALANCED_MODE_P, 0, 0)

        // Send request
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 0, 0, "ping")
        val parsedFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        val constructedPayload = constructPayloadFromFrames(parsedFrames)
        assertEquals("pong${stringUtils.EOL}", constructedPayload)
    }

    @Test
    fun testChunkingOfLongResponse() {
        val mockedTNC = tnc as TNCMocked

        var receivedFrames = ArrayList<KissFrame>()

        // Establish a link
        sendFrameAndWaitResponse(mockedTNC, ControlField.U_SET_ASYNC_BALANCED_MODE_P, 0, 0)

        // Send request for a large response
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 0, 0, "longtest")
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        var lastReceivedFrame = receivedFrames.last()
        while (lastReceivedFrame.controlField() != ControlField.S_8_RECEIVE_READY_P) {
            val nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
            sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY, 0, nextExpectedSendSequenceNumber)
            receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
            lastReceivedFrame = receivedFrames.last()
        }
        val constructedPayload = constructPayloadFromFrames(receivedFrames)
        val expectedResponseString = "${TestApp.longResponseString}${stringUtils.EOL}"
        assertEquals(expectedResponseString, constructedPayload)
    }

    @Test
    fun testSentReceiveSequenceNumbers() {
        val mockedTNC = tnc as TNCMocked

        // Establish a link
        sendFrameAndWaitResponse(mockedTNC, ControlField.U_SET_ASYNC_BALANCED_MODE_P, 0, 0)

        // Send request for a large response
        sendFrame(mockedTNC, ControlField.INFORMATION_8, 0, 0, "longtest")
        // Then pull a VK2VRO special, and send MORE! :-)
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 1, 0, "Hello!")
        val parsedFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        val lastFrame = parsedFrames.last()
        assertEquals(2, lastFrame.receiveSequenceNumber())
    }


    @Test
    fun testFramesWithRepeaterAddresses() {
        /* Send the following frame that includes two repeater addressess, and make sure it's decoded.
           From: VK3EF-9 to: S7U0S1-0 controlType: Via1: VK3RMD-1 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: `I/'p-Wk/
           00 a6 6e aa 60 a6 62 60 ac 96 66 8a 8c 40 72 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 60 49 2f 27 70 2d 57 6b 2f */
        val mockedTNC = tnc as TNCMocked
        sendFrameFromBytesAndWaitResponse(mockedTNC, byteUtils.byteArrayFromInts(0x00, 0xa6, 0x6e, 0xaa, 0x60, 0xa6, 0x62, 0x60, 0xac, 0x96, 0x66, 0x8a, 0x8c, 0x40, 0x72, 0xac, 0x96, 0x66, 0xa4, 0x9a, 0x88, 0xe2, 0xae, 0x92, 0x88, 0x8a, 0x64, 0x40, 0x63, 0x3f))
        assertResponse(byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x8a, 0x8c, 0x40, 0x72, 0xa6, 0x6e, 0xaa, 0x60, 0xa6, 0x62, 0xe1, 0x73, 0xc0), mockedTNC)
    }

}