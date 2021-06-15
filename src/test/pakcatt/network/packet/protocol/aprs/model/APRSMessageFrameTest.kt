package pakcatt.network.packet.protocol.aprs.model

import junit.framework.TestCase
import org.junit.Test
import pakcatt.network.packet.kiss.model.ControlField
import pakcatt.network.packet.kiss.model.ProtocolID
import pakcatt.util.ByteUtils

class APRSMessageFrameTest : TestCase() {

    val byteUtils = ByteUtils()

    @Test
    fun testPopulateFromFrameData() {
        /* Example message from VK3LIT-7 to VK2VRO-7
            From: VK3LIT-7 to: APY03D-0 controlType: Via1: WIDE1-1 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: :VK2VRO-7 :hello{47 */
        val messageFrameBytes = byteUtils.byteArrayFromInts(0x00, 0x82, 0xa0, 0xb2, 0x60, 0x66, 0x88, 0x60, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xee, 0xae, 0x92, 0x88, 0x8a, 0x62, 0x40, 0x62, 0xae, 0x92, 0x88, 0x8a, 0x64, 0x40, 0x63, 0x03, 0xf0, 0x3a, 0x56, 0x4b, 0x32, 0x56, 0x52, 0x4f, 0x2d, 0x37, 0x20, 0x3a, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x7b, 0x34, 0x37, 0x0d)
        val aprsMessageFrame = APRSMessageFrame()
        aprsMessageFrame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, aprsMessageFrame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), aprsMessageFrame.protocolID())
        assertEquals(APRSDataType.MESSAGE, aprsMessageFrame.aprsDataType())
        assertEquals("VK3LIT-7", aprsMessageFrame.sourceCallsign())
        assertEquals("APY03D-0", aprsMessageFrame.destCallsign())
        assertEquals("WIDE1-1", aprsMessageFrame.repeaterCallsignOne())
        assertEquals("WIDE2-1", aprsMessageFrame.repeaterCallsignTwo())
        assertEquals("VK3LIT-7", aprsMessageFrame.messageSourceCallsign())
        assertEquals("VK2VRO-7", aprsMessageFrame.messageDestinationCallsign())
        assertEquals("hello", aprsMessageFrame.message())
        assertEquals(47, aprsMessageFrame.messageNumber())
    }
}