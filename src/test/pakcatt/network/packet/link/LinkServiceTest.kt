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
        mockedTNC.clearDataBuffer()

        // From: VK3LIT-2 to: VK3LIT-1 control: 3f  controlType: U_SET_ASYNC_BALANCED_MODE_P pollFinalBit: 1 protocolID: 80
        val conversationRequest = byteArrayFromInts(0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x62, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x65, 0x3f)

        // From: VK3LIT-2 to: VK3LIT-1 control: 73  controlType: U_TEST pollFinalBit: 1 protocolID: 80 Receive Seq: 3 Send Seq: 1
        val expectedResponse = byteArrayFromInts(0xC0, 0x00, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x64, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe3, 0x73, 0xC0)

        // Send conversation request
        mockedTNC.receiveDataCallback(byteUtils.intToByte(0xC0))
        for (byte in conversationRequest) {
            mockedTNC.receiveDataCallback(byte)
        }
        mockedTNC.receiveDataCallback(byteUtils.intToByte(0xC0))

        // Wait for the response
        await().atMost(Duration.FIVE_SECONDS).until {
            mockedTNC.dataBuffer().size >= 10
        }

        // Evaluate any response
        val response = mockedTNC.dataBuffer()
        val responseStr = stringUtils.byteArrayToHex(response)
        val expectedResponseStr = stringUtils.byteArrayToHex(expectedResponse)
        assertEquals(expectedResponseStr, responseStr)
    }


    private fun byteArrayFromInts(vararg elements: Int): ByteArray {
        val byteArray = ByteArray(elements.size)
        for ((index, intOctet) in elements.withIndex()) {
            val byte = byteUtils.intToByte(intOctet)
            byteArray[index] = byte
        }
        return byteArray
    }

}