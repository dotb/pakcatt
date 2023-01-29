package pakcatt.network.radio.protocol.aprs

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import pakcatt.application.shared.AppInterfaceMocked
import pakcatt.network.radio.protocol.ProtocolTest
import pakcatt.network.radio.protocol.aprs.model.APRSMessageFrame
import pakcatt.network.radio.protocol.aprs.handlers.APRSMessageHandler
import pakcatt.network.radio.protocol.aprs.model.APRSQueue

@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
class APRSServiceTest : ProtocolTest() {

    var appInterface = AppInterfaceMocked()
    var aprsQueue = APRSQueue()
    var aprsFrameHandlers = listOf(APRSMessageHandler("VK3LIT-1", aprsQueue, appInterface, stringUtils))
    val aprsService = APRSService(appInterface, aprsQueue, aprsFrameHandlers)

    @Test
    fun `test messages with ACK numbers`() {
        /*
            Received bytes: 00 82 a0 b2 60 66 88 60 ac 96 66 98 92 a8 ee 82 a0 a4 a6 82 a8 60 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 33 4c 49 54 2d 31 20 3a 6c 61 73 74 7b 37 35 0d
            Received frame: From: VK3LIT-7 to: APY03D-0 controlType: Via1: APRSAT-0 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: :VK3LIT-1 :last{75
        */
        aprsQueue.flushQueue()
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 b2 60 66 88 60 ac 96 66 98 92 a8 ee 82 a0 a4 a6 82 a8 60 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 33 4c 49 54 2d 31 20 3a 6c 61 73 74 7b 37 35 0d")
        val frame = APRSMessageFrame()
        frame.populateFromFrameData(messageFrameBytes)
        frame.channelIdentifier = "test"
        aprsService.handleFrame(frame)
        val responseFrames = aprsQueue.flushQueue()
        assertEquals(2, responseFrames.size)
        assertEquals(":VK3LIT-7 :ack75", responseFrames.first().payloadDataString())
        assertEquals(":VK3LIT-7 :AppRequest(channelIdentifier=test, remoteCallsign=VK3LIT-7, remoteC", responseFrames.last().payloadDataString())
        assertEquals("AppRequest(channelIdentifier=test, remoteCallsign=VK3LIT-7, remoteCallsignWithoutSSID=VK3LIT, addressedToCallsign=VK3LIT-1, message=last, channelIsInteractive=false, viaRepeaterOne=APRSAT-0, viaRepeaterTwo=WIDE2-1, canReceiveMessage=true, location=null, userContext=null)", appInterface.receivedAppRequest.toString())
    }

    @Test
    fun `test messages with single digit ACK numbers`() {
        /*
            Received bytes:00 82 a0 b2 60 66 88 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 33 4c 49 54 2d 31 20 3a 62 6f 61 72 64 2e 6f 70 65 6e 2e 31 2e 6c 69 73 74 7b 30 38 0d
            Received frame:From: VK3LIT-7 to: APY03D-0 controlType: Via1: WIDE1-1 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: :VK3LIT-1 :board.open.1.list{08
        */
        aprsQueue.flushQueue()
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 b2 60 66 88 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 33 4c 49 54 2d 31 20 3a 62 6f 61 72 64 2e 6f 70 65 6e 2e 31 2e 6c 69 73 74 7b 30 38 0d")
        val frame = APRSMessageFrame()
        frame.populateFromFrameData(messageFrameBytes)
        frame.channelIdentifier = "test"
        aprsService.handleFrame(frame)
        val responseFrames = aprsQueue.flushQueue()
        assertEquals(2, responseFrames.size)
        assertEquals(":VK3LIT-7 :ack08", responseFrames.first().payloadDataString())
        assertEquals(":VK3LIT-7 :AppRequest(channelIdentifier=test, remoteCallsign=VK3LIT-7, remoteC", responseFrames.last().payloadDataString())
        assertEquals("AppRequest(channelIdentifier=test, remoteCallsign=VK3LIT-7, remoteCallsignWithoutSSID=VK3LIT, addressedToCallsign=VK3LIT-1, message=board.open.1.list, channelIsInteractive=false, viaRepeaterOne=WIDE1-1, viaRepeaterTwo=WIDE2-1, canReceiveMessage=true, location=null, userContext=null)", appInterface.receivedAppRequest.toString())
    }

}