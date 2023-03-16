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


@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
class NoLayerThreeServiceTest: ProtocolTest() {

    @Autowired
    lateinit var tnc: TNC

    @Test
    fun testFrameDecodesProperly() {
        // In this test, setting the send sequence number is a trick - it should be ignored for S type frames.
        val testFrame = KissFrameStandard()
        testFrame.setDestCallsign("VK3LIT-1")
        testFrame.setSourceCallsign("VK3LIT-2")
        testFrame.setControlField(ControlField.S_8_RECEIVE_READY, 0, 2)
        assertEquals(ControlField.S_8_RECEIVE_READY, testFrame.controlField())
        assertEquals(0, testFrame.receiveSequenceNumber())
    }

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
        var responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        assertEquals(ControlField.INFORMATION_8_P, responseFrames[0].controlField())
        assertEquals("Hi, there! *wave*${stringUtils.EOL}", responseFrames[0].payloadDataString())

        // clear the P Flag
        sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, responseFrames[0].receiveSequenceNumber(), responseFrames[0].sendSequenceNumber(), null, 1)

        /* Receive a disconnect, and respond with an Unnumbered ACK */
        // From: VK3LIT-2 to: VK3LIT-1 control: 53  controlType: U_DISCONNECT_P pollFinalBit: 1 protocolID: 80 Receive Seq: 2 Send Seq: 1
        sendFrameFromBytesAndWaitResponse(mockedTNC, byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe2, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x53))
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1 protocolID: f0 Receive Seq: 3 Send Seq: 1
        assertResponse(byteUtils.byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x63, 0xC0), mockedTNC)
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
            val responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
            rxSequenceNumber = sendSequenceNumber + 1
            sendFrame(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, rxSequenceNumber) // Ack the last I frame
            // Check the INFORMATION Response
            assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", sendSequenceNumber + 1, responseFrames[0].receiveSequenceNumber())
            assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", sendSequenceNumber, responseFrames[0].sendSequenceNumber())
        }

        // The 7th exchange should roll-over the received sequence number
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 7, rxSequenceNumber, "hello")
        var responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        rxSequenceNumber++
        sendFrame(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, rxSequenceNumber) // Ack the last I frame
        // Check the INFORMATION Response
        assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", 0, responseFrames[0].receiveSequenceNumber())
        assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", 7, responseFrames[0].sendSequenceNumber())

        // The 8th exchange should roll-over the sent sequence number
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 0, rxSequenceNumber, "hello")
        responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        sendFrame(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, rxSequenceNumber) // Ack the last I frame
        // Check the INFORMATION Response
        assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", 1, responseFrames[0].receiveSequenceNumber())
        assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", 0, responseFrames[0].sendSequenceNumber())


        // Send another I frame
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8_P, 1, rxSequenceNumber, "hello")
        responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())

        assertEquals("The rxSeq number from the remote party should be one more than the last sendSeq number we've sent.", 2, responseFrames[1].receiveSequenceNumber())
        assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", 1, responseFrames[1].sendSequenceNumber())

        // "Fail" to send a packet:
        /* sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8_P, 2, rxSequenceNumber, "hello")
        / responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer()) */
        // Those 2 lines are the "failed" packet, left here as an annotation

        // Send a RECEIVE_READY_P to follow up the "failed" packet
        sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 2, rxSequenceNumber)
        responseFrames = parseFramesFromResponse(mockedTNC.sentDataBuffer())
        assertEquals("The sendSeq number from the remote party should be the same as the sendSeq number we sent.", 2, responseFrames[0].sendSequenceNumber())
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
        while (!lastReceivedFrame.payloadDataString().contains("quis ornare nibh tempor a.")) {
            val nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
            sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, nextExpectedSendSequenceNumber)
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
        /* Send the following frame that includes two repeater addressess, and make sure it's decoded properly.
           From: VK3EF-9 to: S7U0S1-0 controlType: Via1: VK3RMD-1 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: `I/'p-Wk/
           00 a6 6e aa 60 a6 62 60 ac 96 66 8a 8c 40 72 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 60 49 2f 27 70 2d 57 6b 2f */
        val kissFrame = KissFrameStandard().populateFromFrameData(byteUtils.byteArrayFromStringInts("00 a6 6e aa 60 a6 62 60 ac 96 66 8a 8c 40 72 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 60 49 2f 27 70 2d 57 6b 2f"))
        assertEquals(ControlField.U_UNNUMBERED_INFORMATION,kissFrame.controlField())
        assertEquals("VK3RMD-1",kissFrame.repeaterCallsignOne())
        assertEquals("WIDE2-1",kissFrame.repeaterCallsignTwo())
    }


    @Test
    fun testHandlingWhenSetThePFlagFromARemoteStation() {
        val mockedTNC = tnc as TNCMocked
        val receivedFrames = ArrayList<KissFrame>()

        // Establish a connection
        sendFrameAndWaitResponse(mockedTNC, ControlField.U_SET_ASYNC_BALANCED_MODE_P, 0, 0)
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))

        assertEquals("The remote station requested a connection with the P flag so we must respond with the F bit", ControlField.U_UNNUMBERED_ACKNOWLEDGE_P, receivedFrames[0].controlField())
        assertEquals(1, receivedFrames.size)

        // Send request for a large response
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 0, 0, "longtest", 2)
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))

        assertEquals(ControlField.INFORMATION_8, receivedFrames[1].controlField())
        assertEquals(ControlField.INFORMATION_8_P, receivedFrames[2].controlField())
        assertEquals(3, receivedFrames.size)

        var lastReceivedFrame = receivedFrames.last()
        var nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
        sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, nextExpectedSendSequenceNumber, null, 2) // clear the P Flag
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        lastReceivedFrame = receivedFrames.last()

        assertEquals(ControlField.INFORMATION_8, receivedFrames[3].controlField())
        assertEquals(ControlField.INFORMATION_8_P, receivedFrames[4].controlField())
        assertEquals(5, receivedFrames.size)

        nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
        sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, nextExpectedSendSequenceNumber, null, 2) // clear the P Flag
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        lastReceivedFrame = receivedFrames.last()

        assertEquals(ControlField.INFORMATION_8, receivedFrames[5].controlField())
        assertEquals(ControlField.INFORMATION_8_P, receivedFrames[6].controlField())
        assertEquals(7, receivedFrames.size)

        nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
        sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, nextExpectedSendSequenceNumber, null, 2) // clear the P Flag
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        lastReceivedFrame = receivedFrames.last()

        assertEquals(ControlField.INFORMATION_8, receivedFrames[7].controlField())
        assertEquals(ControlField.INFORMATION_8_P, receivedFrames[8].controlField())
        assertEquals(9, receivedFrames.size)

        nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
        sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, nextExpectedSendSequenceNumber, null, 2) // clear the P Flag
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        lastReceivedFrame = receivedFrames.last()

        assertEquals(ControlField.INFORMATION_8, receivedFrames[9].controlField())
        assertEquals(ControlField.INFORMATION_8_P, receivedFrames[10].controlField())
        assertEquals(11, receivedFrames.size)

        nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
        sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, nextExpectedSendSequenceNumber, null, 1) // clear the P Flag
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        lastReceivedFrame = receivedFrames.last()

        assertEquals("We expect one last information packet with the P flag set", ControlField.INFORMATION_8_P, receivedFrames[11].controlField())
        assertEquals(12, receivedFrames.size)

        nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
        sendFrame(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, nextExpectedSendSequenceNumber) // clear the P Flag
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        lastReceivedFrame = receivedFrames.last()

        // Send a Receive Ready with the P flag set to test sync requests
        nextExpectedSendSequenceNumber = lastReceivedFrame.sendSequenceNumber() + 1
        sendFrameAndWaitResponse(mockedTNC, ControlField.S_8_RECEIVE_READY_P, 0, nextExpectedSendSequenceNumber, null, 1) // request sync
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))
        lastReceivedFrame = receivedFrames.last()

        assertEquals("We expect a RR with the P flag set in response to a sync request", ControlField.S_8_RECEIVE_READY_P, receivedFrames[12].controlField())
        assertEquals(13, receivedFrames.size)

        // Construct and check the final value returned
        val constructedPayload = constructPayloadFromFrames(receivedFrames)
        val expectedResponseString = "${TestApp.longResponseString}${stringUtils.EOL}"
        assertEquals(expectedResponseString, constructedPayload)
    }


    @Test
    fun testConnectionWithRemoteStationWhereNoPFlagIsSet() {
        val mockedTNC = tnc as TNCMocked
        val receivedFrames = ArrayList<KissFrame>()

        // Establish a connection
        sendFrameAndWaitResponse(mockedTNC, ControlField.U_SET_ASYNC_BALANCED_MODE, 0, 0)
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))

        assertEquals("The remote station requested a connection without stting the P flag so we must respond without the F bit", ControlField.U_UNNUMBERED_ACKNOWLEDGE, receivedFrames[0].controlField())
        assertEquals(1, receivedFrames.size)

        // Send request for a large response
        sendFrameAndWaitResponse(mockedTNC, ControlField.INFORMATION_8, 0, 0, "longtest", 2)
        receivedFrames.addAll(parseFramesFromResponse(mockedTNC.sentDataBuffer()))

        assertEquals(ControlField.INFORMATION_8, receivedFrames[1].controlField())
        assertEquals(ControlField.INFORMATION_8_P, receivedFrames[2].controlField())
        assertEquals(3, receivedFrames.size)

    }

}