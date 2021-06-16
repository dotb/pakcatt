package pakcatt.network.radio.protocol.aprs.model

import junit.framework.TestCase
import org.junit.Test
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.ProtocolID
import pakcatt.util.ByteUtils
import pakcatt.util.StringUtils

class APRSMessageFrameTest : TestCase() {

    val byteUtils = ByteUtils()
    val stringUtils = StringUtils()

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

    @Test
    fun testConstructFrameWithParameters() {
        val aprsMessageFrame = APRSMessageFrame()
        aprsMessageFrame.setMessageSourceCallsign("VK3LIT-1")
        aprsMessageFrame.setMessageDestinationCallsign("VK2VRO-7")
        aprsMessageFrame.setMessage("hello there!")
        aprsMessageFrame.setMessageNumber(21)

        val expectedFrameBytes = byteUtils.byteArrayFromInts(0x00, 0xa0, 0x82, 0x96, 0x86, 0x82, 0xa8, 0x60, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0x63, 0x03, 0xf0, 0x3a, 0x56, 0x4b, 0x32, 0x56, 0x52, 0x4f, 0x2d, 0x37, 0x20, 0x3a, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x74, 0x68, 0x65, 0x72, 0x65, 0x21, 0x7b, 0x32, 0x31)
        val expectedPayloadData = byteUtils.byteArrayFromInts(0x3a, 0x56, 0x4b, 0x32, 0x56, 0x52, 0x4f, 0x2d, 0x37, 0x20, 0x3a, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x74, 0x68, 0x65, 0x72, 0x65, 0x21, 0x7b, 0x32, 0x31)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, aprsMessageFrame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), aprsMessageFrame.protocolID())
        assertEquals(APRSDataType.MESSAGE, aprsMessageFrame.aprsDataType())
        assertEquals("VK3LIT-1", aprsMessageFrame.sourceCallsign())
        assertEquals("PAKCAT-0", aprsMessageFrame.destCallsign())
        assertEquals("", aprsMessageFrame.repeaterCallsignOne())
        assertEquals("", aprsMessageFrame.repeaterCallsignTwo())
        assertEquals("VK3LIT-1", aprsMessageFrame.messageSourceCallsign())
        assertEquals("VK2VRO-7", aprsMessageFrame.messageDestinationCallsign())
        assertEquals("hello there!", aprsMessageFrame.message())
        assertEquals(21, aprsMessageFrame.messageNumber())
        assertEquals(":VK2VRO-7 :hello there!{21", aprsMessageFrame.payloadDataString())
        assertEquals(stringUtils.byteArrayToHex(expectedPayloadData), stringUtils.byteArrayToHex(aprsMessageFrame.payloadData()))
        assertEquals(stringUtils.byteArrayToHex(expectedFrameBytes), stringUtils.byteArrayToHex(aprsMessageFrame.packetData()))
    }

    @Test
    fun testConstructFrameWithEdgecaseParameters() {
        val aprsMessageFrame = APRSMessageFrame()
        aprsMessageFrame.setMessageSourceCallsign("VK3LIT-1")
        aprsMessageFrame.setMessageDestinationCallsign("VK3FUNKLONG")
        aprsMessageFrame.setMessage("hello there! This message string is longer than what is allowed by the APRS spec.")
        aprsMessageFrame.setMessageNumber(12345678)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, aprsMessageFrame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), aprsMessageFrame.protocolID())
        assertEquals(APRSDataType.MESSAGE, aprsMessageFrame.aprsDataType())
        assertEquals("VK3LIT-1", aprsMessageFrame.sourceCallsign())
        assertEquals("PAKCAT-0", aprsMessageFrame.destCallsign())
        assertEquals("VK3LIT-1", aprsMessageFrame.messageSourceCallsign())
        assertEquals("VK3FUNKLO", aprsMessageFrame.messageDestinationCallsign())
        assertEquals("hello there! This message string is longer than what is allowed by ", aprsMessageFrame.message())
        assertEquals(12345, aprsMessageFrame.messageNumber())
        assertEquals(":VK3FUNKLO:hello there! This message string is longer than what is allowed by {12345", aprsMessageFrame.payloadDataString())
    }

    @Test
    fun testConstructFrameWithNoMessageIdAndLowCaseCalls() {
        val aprsMessageFrame = APRSMessageFrame()
        aprsMessageFrame.setMessageSourceCallsign("vk3lit-1")
        aprsMessageFrame.setMessageDestinationCallsign("vk2vro")
        aprsMessageFrame.setMessage("hello.")

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, aprsMessageFrame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), aprsMessageFrame.protocolID())
        assertEquals(APRSDataType.MESSAGE, aprsMessageFrame.aprsDataType())
        assertEquals("VK3LIT-1", aprsMessageFrame.sourceCallsign())
        assertEquals("PAKCAT-0", aprsMessageFrame.destCallsign())
        assertEquals("VK3LIT-1", aprsMessageFrame.messageSourceCallsign())
        assertEquals("VK2VRO-0", aprsMessageFrame.messageDestinationCallsign())
        assertEquals("hello.", aprsMessageFrame.message())
        assertEquals(-1, aprsMessageFrame.messageNumber())
        assertEquals(":VK2VRO-0 :hello.", aprsMessageFrame.payloadDataString())
    }

}