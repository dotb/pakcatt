package pakcatt.network.radio.protocol.aprs.model

import junit.framework.TestCase
import org.junit.Test
import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.ProtocolID
import pakcatt.util.ByteUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class APRSStatusFrameTest: TestCase() {

    private val byteUtils = ByteUtils()

    @Test
    fun testStatusFrameWithDate() {
        /*
            Data Type: STATUS From: VK3XJB-1 Payload: >021059zUI-View32 V2.03
            APRS frame data 00 82 a0 aa 64 6a 9c e0 ac 96 66 b0 94 84 62 ac 96 66 a4 9a 8e e2 ac 96 66 a4 9a 88 e3 03 f0 3e 30 32 31 30 35 39 7a 55 49 2d 56 69 65 77 33 32 20 56 32 2e 30 33 0d
        */
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 aa 64 6a 9c e0 ac 96 66 b0 94 84 62 ac 96 66 a4 9a 8e e2 ac 96 66 a4 9a 88 e3 03 f0 3e 30 32 31 30 35 39 7a 55 49 2d 56 69 65 77 33 32 20 56 32 2e 30 33 0d")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)
        val dateTimeNow = LocalDateTime.now()
        var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val expectedDateTimeString = "${dateFormatter.format(dateTimeNow)}-02T10:59"

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3XJB-1", frame.sourceCallsign())
        assertEquals("VK3RMG-1", frame.repeaterCallsignOne())
        assertEquals("VK3RMD-1", frame.repeaterCallsignTwo())
        assertEquals("UI-View32 V2.03", frame.statusText())
        assertEquals(expectedDateTimeString, frame.dateTimeStamp().toString())
        assertEquals(null, frame.beamHeadingAndERP())
        assertEquals(null, frame.maidenheadGridLocator())
        assertEquals(null, frame.symbolCode())
        assertEquals(null, frame.symbolTableId())
    }

    @Test
    fun testStatusOnly() {
        /*
            Data Type: STATUS From: VK3DPW-1 Payload: >/VK3DPW - WICEN Testing
            APRS frame data 00 82 a0 a8 a8 68 40 60 ac 96 66 88 a0 ae 62 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 3e 2f 56 4b 33 44 50 57 20 2d 20 57 49 43 45 4e 20 54 65 73 74 69 6e 67
        */
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 a8 a8 68 40 60 ac 96 66 88 a0 ae 62 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 3e 2f 56 4b 33 44 50 57 20 2d 20 57 49 43 45 4e 20 54 65 73 74 69 6e 67")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3DPW-1", frame.sourceCallsign())
        assertEquals("VK3RMD-1", frame.repeaterCallsignOne())
        assertEquals("WIDE2-1", frame.repeaterCallsignTwo())
        assertEquals("/VK3DPW - WICEN Testing", frame.statusText())
        assertEquals(null, frame.dateTimeStamp())
        assertEquals(null, frame.beamHeadingAndERP())
        assertEquals(null, frame.maidenheadGridLocator())
        assertEquals(null, frame.symbolCode())
        assertEquals(null, frame.symbolTableId())
    }

    @Test
    fun testStatusWithHeadingAndERP() {
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 a8 a8 68 40 60 ac 96 66 88 a0 ae 62 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 3e 2f 56 4b 33 44 50 57 20 2d 20 57 49 43 45 4e 20 54 65 73 74 69 6e 67 5e 42 37")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)
        val expectedHeadingAndERP = HeadingAndERP(110, 490)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3DPW-1", frame.sourceCallsign())
        assertEquals("VK3RMD-1", frame.repeaterCallsignOne())
        assertEquals("WIDE2-1", frame.repeaterCallsignTwo())
        assertEquals("/VK3DPW - WICEN Testing", frame.statusText())
        assertEquals(null, frame.dateTimeStamp())
        assertEquals(expectedHeadingAndERP, frame.beamHeadingAndERP())
        assertEquals(null, frame.maidenheadGridLocator())
        assertEquals(null, frame.symbolCode())
        assertEquals(null, frame.symbolTableId())
    }

    @Test
    fun testStatusFrameWithDateAndHeadingAndERP() {
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 aa 64 6a 9c e0 ac 96 66 b0 94 84 62 ac 96 66 a4 9a 8e e2 ac 96 66 a4 9a 88 e3 03 f0 3e 30 32 31 30 35 39 7a 55 49 2d 56 69 65 77 33 32 20 56 32 2e 30 33 0d 5e 42 37")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)
        val dateTimeNow = LocalDateTime.now()
        var dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val expectedDateTimeString = "${dateFormatter.format(dateTimeNow)}-02T10:59"
        val expectedHeadingAndERP = HeadingAndERP(110, 490)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3XJB-1", frame.sourceCallsign())
        assertEquals("VK3RMG-1", frame.repeaterCallsignOne())
        assertEquals("VK3RMD-1", frame.repeaterCallsignTwo())
        assertEquals("UI-View32 V2.03", frame.statusText())
        assertEquals(expectedDateTimeString, frame.dateTimeStamp().toString())
        assertEquals(expectedHeadingAndERP, frame.beamHeadingAndERP())
        assertEquals(null, frame.maidenheadGridLocator())
        assertEquals(null, frame.symbolCode())
        assertEquals(null, frame.symbolTableId())
    }

    @Test
    fun testStatusAndLongMaidenheadAndSymbol() {
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 a8 a8 68 40 60 ac 96 66 88 a0 ae 62 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 3e 49 4f 39 31 53 58 2f 47 20 2f 56 4b 33 44 50 57 20 2d 20 57 49 43 45 4e 20 54 65 73 74 69 6e 67")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3DPW-1", frame.sourceCallsign())
        assertEquals("VK3RMD-1", frame.repeaterCallsignOne())
        assertEquals("WIDE2-1", frame.repeaterCallsignTwo())
        assertEquals("/VK3DPW - WICEN Testing", frame.statusText())
        assertEquals(null, frame.dateTimeStamp())
        assertEquals(null, frame.beamHeadingAndERP())
        assertEquals("IO91SX", frame.maidenheadGridLocator())
        assertEquals("/", frame.symbolCode())
        assertEquals("G", frame.symbolTableId())
    }

    @Test
    fun testStatusAndShortMaidenheadAndSymbol() {
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 a8 a8 68 40 60 ac 96 66 88 a0 ae 62 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 3e 49 4f 39 31 2f 47 20 2f 56 4b 33 44 50 57 20 2d 20 57 49 43 45 4e 20 54 65 73 74 69 6e 67")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3DPW-1", frame.sourceCallsign())
        assertEquals("VK3RMD-1", frame.repeaterCallsignOne())
        assertEquals("WIDE2-1", frame.repeaterCallsignTwo())
        assertEquals("/VK3DPW - WICEN Testing", frame.statusText())
        assertEquals(null, frame.dateTimeStamp())
        assertEquals(null, frame.beamHeadingAndERP())
        assertEquals("IO91", frame.maidenheadGridLocator())
        assertEquals("/", frame.symbolCode())
        assertEquals("G", frame.symbolTableId())
    }

    @Test
    fun testLongMaidenheadOnly() {
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 a8 a8 68 40 60 ac 96 66 88 a0 ae 62 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 3e 49 4f 39 31 53 58 2f 47 20")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3DPW-1", frame.sourceCallsign())
        assertEquals("VK3RMD-1", frame.repeaterCallsignOne())
        assertEquals("WIDE2-1", frame.repeaterCallsignTwo())
        assertEquals("", frame.statusText())
        assertEquals(null, frame.dateTimeStamp())
        assertEquals(null, frame.beamHeadingAndERP())
        assertEquals("IO91SX", frame.maidenheadGridLocator())
        assertEquals("/", frame.symbolCode())
        assertEquals("G", frame.symbolTableId())
    }

    @Test
    fun testShortMaidenheadOnly() {
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 a8 a8 68 40 60 ac 96 66 88 a0 ae 62 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 3e 49 4f 39 31 2f 47")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3DPW-1", frame.sourceCallsign())
        assertEquals("VK3RMD-1", frame.repeaterCallsignOne())
        assertEquals("WIDE2-1", frame.repeaterCallsignTwo())
        assertEquals("", frame.statusText())
        assertEquals(null, frame.dateTimeStamp())
        assertEquals(null, frame.beamHeadingAndERP())
        assertEquals("IO91", frame.maidenheadGridLocator())
        assertEquals("/", frame.symbolCode())
        assertEquals("G", frame.symbolTableId())
    }

    @Test
    fun testStatusAndLongMaidenheadAndSymbolAndHeadingAndERP() {
        val messageFrameBytes = byteUtils.byteArrayFromStringInts("00 82 a0 a8 a8 68 40 60 ac 96 66 88 a0 ae 62 ac 96 66 a4 9a 88 e2 ae 92 88 8a 64 40 63 03 f0 3e 49 4f 39 31 53 58 2f 47 20 2f 56 4b 33 44 50 57 20 2d 20 57 49 43 45 4e 20 54 65 73 74 69 6e 67 5e 42 37")
        val frame = APRSStatusFrame()
        frame.populateFromFrameData(messageFrameBytes)
        val expectedHeadingAndERP = HeadingAndERP(110, 490)

        assertEquals(ControlField.U_UNNUMBERED_INFORMATION, frame.controlField())
        assertEquals(byteUtils.intToByte(ProtocolID.NO_LAYER_3.id), frame.protocolID())
        assertEquals(APRSDataType.STATUS, frame.aprsDataType())
        assertEquals("VK3DPW-1", frame.sourceCallsign())
        assertEquals("VK3RMD-1", frame.repeaterCallsignOne())
        assertEquals("WIDE2-1", frame.repeaterCallsignTwo())
        assertEquals("/VK3DPW - WICEN Testing", frame.statusText())
        assertEquals(null, frame.dateTimeStamp())
        assertEquals(expectedHeadingAndERP, frame.beamHeadingAndERP())
        assertEquals("IO91SX", frame.maidenheadGridLocator())
        assertEquals("/", frame.symbolCode())
        assertEquals("G", frame.symbolTableId())
    }

}