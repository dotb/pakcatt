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
    fun testCQBroadcast() {
        val mockCQFrameInts = intArrayOf(0x00, 0x86, 0xa2, 0x40, 0x40, 0x40, 0x40, 0xe0, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x03, 0xf0, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x0d)
        for (intOctet in mockCQFrameInts) {
            val receivedByte = ByteUtils().intToByte(intOctet)
//            mockedTNC.receiveDataCallback(receivedByte)
        }
    }

    @Test
    fun testStandardConversationHandshake() {
        val mockedTNC = tnc as TNCMocked


        /* Accept an incoming conversation, respond with an Unnumbered ACK */
        mockedTNC.clearDataBuffer()
        // From: VK3LIT-2 to: VK3LIT-1 control: 3f  controlType: U_SET_ASYNC_BALANCED_MODE_P pollFinalBit: 1 protocolID: 80
        sendFrame(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x62, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x3f), mockedTNC)
        waitForResponse(mockedTNC)
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1
        assertResponse(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0), mockedTNC)


        /* Receive an incoming message, respond with an Ready Receive ACK*/
        mockedTNC.clearDataBuffer()
        // From: VK3LIT-2 to: VK3LIT-1 control: 10  controlType: I_8 pollFinalBit: 1 protocolID: f0 Receive Seq: 0 Send Seq: 0 Data: Hello!
        sendFrame(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe2, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x10, 0xf0, 0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x21, 0x0d), mockedTNC)
        waitForResponse(mockedTNC)
        // From: VK3LIT-1 to: VK3LIT-2 control: 21  controlType: S_8_RECEIVE_READY pollFinalBit: 0 protocolID: f0 Receive Seq: 1 Send Seq: 0
        assertResponse(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x31, 0xC0,
        // From: VK3LIT-1 to: VK3LIT-2 control: 20  controlType: I_8 pollFinalBit: 0 protocolID: f0 Receive Seq: 1 Send Seq: 0 -  Hi, there! *wave*
        0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe4, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x63, 0x20, 0xf0, 0x48, 0x69, 0x2c, 0x20, 0x74, 0x68, 0x65, 0x72, 0x65, 0x21, 0x20, 0x2a, 0x77, 0x61, 0x76, 0x65, 0x2a, 0xc0), mockedTNC)

        /* Receive an Receive Ready P message and transfer rx variable state back to the remote TNC */
        mockedTNC.clearDataBuffer()
        // From: VK3LIT-2 to: VK3LIT-1 control: 10  controlType: I_8 pollFinalBit: 1 protocolID: f0 Receive Seq: 0 Send Seq: 0 Data: Hello!
        sendFrame(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe2, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x11), mockedTNC)
        waitForResponse(mockedTNC)
        // From: VK3LIT-2 to: VK3LIT-1 control: 73  controlType: U_TEST pollFinalBit: 1 protocolID: 80 Receive Seq: 3 Send Seq: 1
        assertResponse(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x31, 0xC0), mockedTNC)



        /* Receive a disconnect, and respond with an Unnumbered ACK */
        mockedTNC.clearDataBuffer()
        // From: VK3LIT-2 to: VK3LIT-1 control: 53  controlType: U_DISCONNECT_P pollFinalBit: 1 protocolID: 80 Receive Seq: 2 Send Seq: 1
        sendFrame(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe2, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x53), mockedTNC)
        waitForResponse(mockedTNC)
        // From: VK3LIT-1 to: VK3LIT-2 control: 73  controlType: U_UNNUMBERED_ACKNOWLEDGE_P pollFinalBit: 1 protocolID: f0 Receive Seq: 3 Send Seq: 1
        assertResponse(byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0), mockedTNC)
    }


    private fun byteArrayFromInts(vararg elements: Int): ByteArray {
        val byteArray = ByteArray(elements.size)
        for ((index, intOctet) in elements.withIndex()) {
            val byte = byteUtils.intToByte(intOctet)
            byteArray[index] = byte
        }
        return byteArray
    }

    private fun sendFrame(frameData: ByteArray, mockedTNC: TNCMocked) {
        // Send conversation request
        mockedTNC.receiveDataCallback(byteUtils.intToByte(0xC0))
        for (byte in frameData) {
            mockedTNC.receiveDataCallback(byte)
        }
        mockedTNC.receiveDataCallback(byteUtils.intToByte(0xC0))
    }

    private fun waitForResponse(mockedTNC: TNCMocked) {
        // Wait for the response
        await().atMost(Duration.TEN_SECONDS).until {
            mockedTNC.dataBuffer().size >= 10
        }
    }

    private fun assertResponse(expectedResponse: ByteArray, mockedTNC: TNCMocked) {
        val response = mockedTNC.dataBuffer()
        assertEquals(stringUtils.byteArrayToHex(expectedResponse), stringUtils.byteArrayToHex(response))
    }

}