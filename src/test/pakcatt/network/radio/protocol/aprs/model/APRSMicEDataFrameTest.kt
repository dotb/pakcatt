package pakcatt.network.radio.protocol.aprs.model

import junit.framework.TestCase
import org.junit.Test
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.ProtocolID
import pakcatt.util.ByteUtils

class APRSMicEDataFrameTest: TestCase() {

    private val byteUtils = ByteUtils()

    @Test
    fun testPopulateFromFrameData() {
        /*
            Received bytes:	 00 88 90 68 98 b4 98 60 ac 96 66 98 92 a8 ee ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 60 48 54 7c 6c 21 5f 5b 2f 60 22 34 45 7d 31 34 36 2e 34 35 30 4d 48 7a 20 2d 20 42 72 61 64 20 2d 20 76 6b 33 6c 69 74 2e 63 6f 6d 5f 30 0d
            Received frame:	 From: VK3LIT-7 to: DH4LZL-0 controlType: Via1: WIDE1-1 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: `HT|l!_[/`"4E}146.450MHz - Brad - vk3lit.com_0
            Decoded data:	 `HT|l!_[/`"4E}146.450MHz - Brad - vk3lit.com_0
        */
        val messageFrameBytes = byteUtils.byteArrayFromInts(0x00, 0x88, 0x90, 0x68, 0x98, 0xb4, 0x98, 0x60, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xee, 0xae, 0x92, 0x88, 0x8a, 0x62, 0x40, 0x62, 0xae, 0x92, 0x88, 0x8a, 0x64, 0x40, 0x63, 0x03, 0xf0, 0x60, 0x48, 0x54, 0x7c, 0x6c, 0x21, 0x5f, 0x5b, 0x2f, 0x60, 0x22, 0x34, 0x45, 0x7d, 0x31, 0x34, 0x36, 0x2e, 0x34, 0x35, 0x30, 0x4d, 0x48, 0x7a, 0x20, 0x2d, 0x20, 0x42, 0x72, 0x61, 0x64, 0x20, 0x2d, 0x20, 0x76, 0x6b, 0x33, 0x6c, 0x69, 0x74, 0x2e, 0x63, 0x6f, 0x6d, 0x5f, 0x30, 0x0d)
        val aprsMicEDataFrame = APRSMicEDataFrame()
        aprsMicEDataFrame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, aprsMicEDataFrame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), aprsMicEDataFrame.protocolID())
        assertEquals(APRSDataType.MIC_E_DATA, aprsMicEDataFrame.aprsDataType())
        assertEquals("VK3LIT-7", aprsMicEDataFrame.sourceCallsign())
        assertEquals("WIDE1-1", aprsMicEDataFrame.repeaterCallsignOne())
        assertEquals("WIDE2-1", aprsMicEDataFrame.repeaterCallsignTwo())
        assertEquals(MIC_E.CUSTOM_1, aprsMicEDataFrame.micEType())
        assertEquals(3, aprsMicEDataFrame.ambiguity())
        assertEquals(100, aprsMicEDataFrame.longitudeOffset())
        assertEquals("37.40.00S", aprsMicEDataFrame.latitudeDegreesMinutesHundredths())
        assertEquals("144.50.00E", aprsMicEDataFrame.longitudeDegreesMinutesHundredths())
        assertEquals(0.0, aprsMicEDataFrame.speedKnots())
        assertEquals(0.0, aprsMicEDataFrame.speedKmh())
        assertEquals(167, aprsMicEDataFrame.courseDegrees())
        assertEquals("146.450MHz - Brad - vk3lit.com", aprsMicEDataFrame.statusText())
    }


    @Test
    fun testSpeedAndDirection() {
        /*
            Received bytes:	 00 88 90 8a 6c a0 6c 60 ac 96 66 98 92 a8 e4 ae 92 88 8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 60 48 55 32 6e 20 7e 3e 2f 60 22 34 4c 7d 31 34 36 2e 34 35 30 4d 48 7a 20 42 72 61 64 5f 31 0d
            Received frame:	 From: VK3LIT-2 to: DHE6P6-0 controlType: Via1: WIDE1-1 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: `HU2n ~>/`"4L}146.450MHz Brad_1
            Decoded data:	 `HU2n ~>/`"4L}146.450MHz Brad_1
        */
        val messageFrameBytes = byteUtils.byteArrayFromInts(0x00, 0x88, 0x90, 0x8a, 0x6c, 0xa0, 0x6c, 0x60, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe4, 0xae, 0x92, 0x88, 0x8a, 0x62, 0x40, 0x62, 0xae, 0x92, 0x88, 0x8a, 0x64, 0x40, 0x63, 0x03, 0xf0, 0x60, 0x48, 0x55, 0x32, 0x6e, 0x20, 0x7e, 0x3e, 0x2f, 0x60, 0x22, 0x34, 0x4c, 0x7d, 0x31, 0x34, 0x36, 0x2e, 0x34, 0x35, 0x30, 0x4d, 0x48, 0x7a, 0x20, 0x42, 0x72, 0x61, 0x64, 0x5f, 0x31, 0x0d)
        val aprsMicEDataFrame = APRSMicEDataFrame()
        aprsMicEDataFrame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, aprsMicEDataFrame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), aprsMicEDataFrame.protocolID())
        assertEquals(APRSDataType.MIC_E_DATA, aprsMicEDataFrame.aprsDataType())
        assertEquals("VK3LIT-2", aprsMicEDataFrame.sourceCallsign())
        assertEquals("WIDE1-1", aprsMicEDataFrame.repeaterCallsignOne())
        assertEquals("WIDE2-1", aprsMicEDataFrame.repeaterCallsignTwo())
        assertEquals(MIC_E.CUSTOM_0, aprsMicEDataFrame.micEType())
        assertEquals(0, aprsMicEDataFrame.ambiguity())
        assertEquals(100, aprsMicEDataFrame.longitudeOffset())
        assertEquals("37.46.06S", aprsMicEDataFrame.latitudeDegreesMinutesHundredths())
        assertEquals("144.57.22E", aprsMicEDataFrame.longitudeDegreesMinutesHundredths())
        assertEquals(20.0, aprsMicEDataFrame.speedKnots())
        assertEquals(37.04002, aprsMicEDataFrame.speedKmh())
        assertEquals(98, aprsMicEDataFrame.courseDegrees())
        assertEquals("146.450MHz Brad", aprsMicEDataFrame.statusText())
    }

    @Test
    fun testAmbiguity() {
        /*
            Received frame:	 From: VK3LIT-2 to: SW34ZL-0 controlType: Via1: WIDE1-1 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: `HFmSw>/`"6W}Just cruising ^:-}_"
            Typed APRS Frame: Data Type: MIC_E_DATA From: VK3TKK-9 Lat: 37.34.00S Lon: 144.42.00E Ambiguity: 2 Speed: 27.780015km/h Knots: 15.0 Course: 191 Status: _" Payload: `HFmSw>/`"6W}Just cruising ^:-}_"
        */
        val messageFrameBytes = byteUtils.byteArrayFromInts(0x00, 0xa6, 0xae, 0x66, 0x68, 0xb4, 0x98, 0x60, 0xac, 0x96, 0x66, 0x98, 0x92, 0xa8, 0xe4, 0xae, 0x92, 0x88, 0x8a, 0x62, 0x40, 0x62, 0xae, 0x92, 0x88, 0x8a, 0x64, 0x40, 0x63, 0x03, 0xf0, 0x60, 0x48, 0x46, 0x1c, 0x6d, 0x53, 0x77, 0x3e, 0x2f, 0x60, 0x22, 0x36, 0x57, 0x7d, 0x4a, 0x75, 0x73, 0x74, 0x20, 0x63, 0x72, 0x75, 0x69, 0x73, 0x69, 0x6e, 0x67, 0x20, 0x5e, 0x3a, 0x2d, 0x7d, 0x5f, 0x22, 0x0d)
        val aprsMicEDataFrame = APRSMicEDataFrame()
        aprsMicEDataFrame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, aprsMicEDataFrame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), aprsMicEDataFrame.protocolID())
        assertEquals(APRSDataType.MIC_E_DATA, aprsMicEDataFrame.aprsDataType())
        assertEquals("VK3LIT-2", aprsMicEDataFrame.sourceCallsign())
        assertEquals("WIDE1-1", aprsMicEDataFrame.repeaterCallsignOne())
        assertEquals("WIDE2-1", aprsMicEDataFrame.repeaterCallsignTwo())
        assertEquals(MIC_E.EN_ROUTE, aprsMicEDataFrame.micEType())
        assertEquals(2, aprsMicEDataFrame.ambiguity())
        assertEquals(100, aprsMicEDataFrame.longitudeOffset())
        assertEquals("37.34.00S", aprsMicEDataFrame.latitudeDegreesMinutesHundredths())
        assertEquals("144.42.00E", aprsMicEDataFrame.longitudeDegreesMinutesHundredths())
        assertEquals(15.0, aprsMicEDataFrame.speedKnots())
        assertEquals(27.780015, aprsMicEDataFrame.speedKmh())
        assertEquals(191, aprsMicEDataFrame.courseDegrees())
        assertEquals("Just cruising ^:-}", aprsMicEDataFrame.statusText())
    }

}