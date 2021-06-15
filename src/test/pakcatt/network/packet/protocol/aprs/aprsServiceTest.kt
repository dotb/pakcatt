package pakcatt.network.packet.protocol.aprs

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import pakcatt.network.packet.protocol.ProtocolTest
import pakcatt.network.packet.tnc.TNC
import pakcatt.network.packet.tnc.TNCMocked

@ActiveProfiles("test")
@SpringBootTest
@RunWith(SpringJUnit4ClassRunner::class)
class aprsServiceTest : ProtocolTest() {

    @Autowired
    lateinit var tnc: TNC

    @Test
    fun testHandleFrame() {
        val mockedTNC = tnc as TNCMocked
        /* Example message from VK3LIT-7 to VK2VRO-7
        Received frame:	 Receive Seq: 5 Send Seq: 7 Delivery Attempt: 0 protocolID: 92 From: VK3LIT-7 to: APY03D-0 controlType: INFORMATION_8 Payload: ��b@b����d@c�:VK2VRO-7 :hello{47
        Received bytes:	 00   82 a0 b2 60 66 88 60   ac 96 66 98 92 a8 ee   ae   92   88   8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d */
        sendFrameFromBytes(mockedTNC, byteUtils.byteArrayFromInts(0x00, 0x82, 0xa0, 0xb2, 0x60, 0x66, 0x88, 0x60, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa, 0xee, 0xae, 0x92, 0x88, 0x8a, 0x62, 0x40, 0x62, 0xae, 0x92, 0x88, 0x8a, 0x64, 0x40, 0x63, 0x03, 0xf0, 0x3a, 0x56, 0x4b, 0x32, 0x56, 0x52, 0x4f, 0x2d, 0x37, 0x20, 0x3a, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x7b, 0x34, 0x37, 0x0d))
    }

}