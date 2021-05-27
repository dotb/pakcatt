package pakcatt.tnc.kiss

import org.slf4j.LoggerFactory
import pakcatt.util.ByteUtils
import pakcatt.util.StringUtils
import kotlin.experimental.and

/*
# KISS and embedded AX.25 frames contain these fields #
Frame End (FEND)   1 byte (0xc0) (KISS)
Port & Command     1 byte
                    - port is first nibble (0x00 - 0x09) (KISS)
                    - command is second nibble (0x00 - 0x06, 0xFF) (KISS)

## AX.25 Data component ##
### Addressing ###
Dest Callsign      6 bytes (Callsign using 7 bit uppercase chars) (AX.25)
Dest SSID          1 byte (CRRSSID0 C=Command, R=Reserved, SSID=SSID, 0=Extension bit that is always 0) (AX.25)
Source Callsign    6 bytes (Callsign using 7 bit uppercase chars) (AX.25)
Source SSID        1 byte (CRRSSID1 C=Command, R=Reserved, SSID=SSID, 0=Extension bit) (AX.25)
                    - The last octet of the address field has an extension bit set:
                    - 0 = more address data to follow or,
                    - 1 = no more address data to follow) (AX.25)

### Optional Repeater addressing ###
The repeater section can be repeated (pun intended) to include up to two repeaters.
Although the documentation seems to indicate it could include more repeaters.
Repeater Callsign  6 bytes (Callsign using 7 bit uppercase chars) (AX.25)
Repeater SSID      1 byte (HRRSSID1 H=Repeated yes/no, R=Reserved, SSID=SSID, 1=Extension bit) (AX.25)
                    - H = 1 Has been repeated
                    - H = 0 has not been repeated
                    - The repeater extension bit is always 1 = no more address data to follow) (AX.25)


Control field      1 byte (AX.25)
                    - I or Information frame - RRRPSSS0 (0  = I)
                    - S or Supervisory frame - RRRPFF01 (01 = S)
                    - U or Unnumbered frame  - MMMPMM11 (11 = U)
                        Where
                        - RRR = Receive sequence number - senders next expected receive number
                        - P   = Poll/Final bit
                        - SSS = Send sequence number - senders current send number, for this frame
                        - FF = Supervisory function bits

Protocol ID        1 byte (always 0xf0 - no layer 3 protocol) (AX.25)
Information Field  1-256 bytes (payload data) (AX.25)

Frame End (FEND)   1 byte (0xc0) (KISS)
 */

class KissFrame() {

    companion object {
        const val FRAME_END = -64
        const val SIZE_MIN = 15
    }

    enum class ControlFrame {
        I_FRAME,
        S_FRAME_RECEIVE_READY, S_FRAME_RECEIVE_NOT_READY, S_FRAME_REJECT, S_FRAME_SELECTIVE_REJECT,
        U_FRAME_SET_ASYNC_BALANCED_MODE, U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED, U_FRAME_DISCONNECT,
        U_FRAME_DISCONNECT_MODE, U_FRAME_UNNUMBERED_ACKNOWLEDGE, U_FRAME_REJECT, U_FRAME_UNNUMBERED_INFORMATION,
        U_FRAME_EXCHANGE_IDENTIFICATION, U_FRAME_TEST,
        UNKNOWN_FRAME
    }

    private val logger = LoggerFactory.getLogger(KissFrame::class.java)
    private var portAndCommand: Byte = 0x00
    private var destCallsign: ByteArray = ByteArray(0)
    private var destSSID: Byte = 0x00
    private var sourceCallsign: ByteArray = ByteArray(0)
    private var sourceSSID: Byte = 0x00
    private var controlField: Byte = 0x00
    private var protocolID: Byte = 0xF0.toByte()
    private var payloadData: ByteArray = ByteArray(0)

    fun parseRawKISSFrame(portAndCommand: Byte,
                          destCallsign: ByteArray,
                          destSSID: Byte,
                          sourceCallsign: ByteArray,
                          sourceSSID: Byte,
                          controlField: Byte,
                          protocolID: Byte,
                          payloadData: ByteArray) {
        this.portAndCommand = portAndCommand
        this.destCallsign = destCallsign
        this.destSSID = destSSID
        this.sourceCallsign = sourceCallsign
        this.sourceSSID = sourceSSID
        this.controlField = controlField
        this.protocolID = protocolID
        this.payloadData = payloadData
    }

    fun setDestCallsign(destCallsign: String) {
        val parsedCallsign = parseStringCallsign(destCallsign)
        this.destCallsign = parsedCallsign.first
        this.destSSID = parsedCallsign.second
    }

    fun setSourceCallsign(sourceCallsign: String) {
        val parsedCallsign = parseStringCallsign(sourceCallsign)
        this.sourceCallsign = parsedCallsign.first
        this.sourceSSID = parsedCallsign.second
    }

    fun setPayloadMessage(message: String) {
        payloadData = StringUtils.convertStringToBytes(message)
    }

    fun setReceiveSequenceNumber(receiveSeq: Int) {
        val sequenceNumberByte = parseReceiveSequenceNumber(receiveSeq)
        controlField = controlField.and(sequenceNumberByte)
    }

    fun setSendSequenceNumber(sendSeq: Int) {
        val sequenceNumberByte = parseSendSequenceNumber(sendSeq)
        controlField = controlField.and(sequenceNumberByte)
    }

    fun setPollFinalBit(pollFinalBit: Boolean) {
        if (pollFinalBit) {
            controlField = ByteUtils.setBits(controlField, 0x10)
        } else {
            controlField = ByteUtils.maskByte(controlField, 0xDF)
        }
    }

    fun packetData(): ByteArray {
        val packetSize = 7 + destCallsign.size + sourceCallsign.size + payloadData.size
        var kissPacket = ByteArray(packetSize)
        kissPacket[0] = portAndCommand
        ByteUtils.insertIntoByteArray(destCallsign, kissPacket, 1)
        kissPacket[7] = destSSID
        ByteUtils.insertIntoByteArray(sourceCallsign, kissPacket, 8)
        kissPacket[14] = sourceSSID
        kissPacket[15] = controlField
        kissPacket[16] = protocolID
        ByteUtils.insertIntoByteArray(payloadData, kissPacket, 17)
        return kissPacket
    }

    fun sourceCallsign(): String {
        return constructCallsign(sourceCallsign, sourceSSID)
    }

    fun destCallsign(): String {
        return constructCallsign(destCallsign, destSSID)
    }

    fun controlField(): Byte {
        return controlField
    }

    fun protocolID(): Byte {
        return protocolID
    }

    fun payloadData(): ByteArray {
        return payloadData
    }

    fun payloadDataString(): String {
        return StringUtils.convertBytesToString(payloadData)
    }

    fun controlFrame(): ControlFrame {
        return calculateControlFrame()
    }

    fun receiveSequenceNumber(): Int {
        return calculateReceiveSequenceNumber()
    }

    fun sendSequenceNumber(): Int {
        return calculateSendSequenceNumber()
    }

    fun controlTypeString(): String {
        return when (calculateControlFrame()) {
            ControlFrame.I_FRAME -> "I"
            ControlFrame.S_FRAME_RECEIVE_READY -> "S_RECEIVE_READY"
            ControlFrame.S_FRAME_RECEIVE_NOT_READY -> "S_RECEIVE_NOT_READY"
            ControlFrame.S_FRAME_REJECT -> "S_REJECT"
            ControlFrame.S_FRAME_SELECTIVE_REJECT -> "S_SELECTIVE_REJECT"
            ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE -> "U_ASYNC_BAL_MODE"
            ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED -> "U_ASYNC_BAL_MODE_EX"
            ControlFrame.U_FRAME_DISCONNECT -> "U_DISCONNECT"
            ControlFrame.U_FRAME_DISCONNECT_MODE -> "U_DISCONNECT_MODE"
            ControlFrame.U_FRAME_UNNUMBERED_ACKNOWLEDGE -> "U_UNNUMBERED_ACK"
            ControlFrame.U_FRAME_REJECT -> "U_REJECT"
            ControlFrame.U_FRAME_UNNUMBERED_INFORMATION -> "U_UNNUMBERED_INFO"
            ControlFrame.U_FRAME_EXCHANGE_IDENTIFICATION -> "U_EXCHANGE_ID"
            ControlFrame.U_FRAME_TEST -> "U_TEST"
            ControlFrame.UNKNOWN_FRAME -> "?"
        }
    }

    fun pollFinalBit(): Boolean {
        return ByteUtils.compareMaskedByte(controlField,0x10, 0x10)
    }

    fun pollFinalBitString(): String {
        return when (pollFinalBit()) {
            true -> "1"
            false -> "0"
        }
    }

    override fun toString(): String {
        return "From: ${sourceCallsign()} to: ${destCallsign()} control: ${StringUtils.byteToHex(controlField())} " +
                "controlType: ${controlTypeString()} pollFinalBit: ${pollFinalBitString()} protocolID: ${StringUtils.byteToHex(protocolID())} " +
                "Receive Seq: ${receiveSequenceNumber()} Send Seq: ${sendSequenceNumber()}"
    }

    private fun constructCallsign(callsignByteArray: ByteArray, callsignSSID: Byte): String {
        val shiftedCallsign = ByteUtils.shiftBitsRight(callsignByteArray, 1)
        val callsignString = StringUtils.convertBytesToString(shiftedCallsign)
        val trimmedCallsign = StringUtils.removeWhitespace(callsignString)
        val ssid = ssidFromSSIDByte(callsignSSID)
        return "${trimmedCallsign}-${ssid}"
    }

    private fun ssidFromSSIDByte(ssidByte: Byte): Int {
        val shiftedByte = ByteUtils.shiftBitsRight(ssidByte, 1)
        return ByteUtils.maskInt(shiftedByte.toInt(), 0x0F)
    }

    /*
    Control field      1 byte (AX.25)
    - I or Information frame - RRRPSSS0 (0  = I)
    - S or Supervisory frame - RRRPFF01 (01 = S)
    - U or Unnumbered frame  - MMMPMM11 (11 = U)
        Where
        - RRR = Receive sequence number - senders next expected receive number
        - P   = Poll/Final bit
        - SSS = Send sequence number - senders current send number, for this frame
        - FF = Supervisory function bits
     */
    private fun calculateControlFrame(): ControlFrame {
        return when {
            ByteUtils.compareMaskedByte(controlField,0x01, 0x00) -> { ControlFrame.I_FRAME }
            ByteUtils.compareMaskedByte(controlField,0x0F, 0x01) -> { ControlFrame.S_FRAME_RECEIVE_READY }
            ByteUtils.compareMaskedByte(controlField,0x0F, 0x05) -> { ControlFrame.S_FRAME_RECEIVE_NOT_READY }
            ByteUtils.compareMaskedByte(controlField,0x0F, 0x09) -> { ControlFrame.S_FRAME_REJECT }
            ByteUtils.compareMaskedByte(controlField,0x0F, 0x0D) -> { ControlFrame.S_FRAME_SELECTIVE_REJECT }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0x6F) -> { ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0x2F) -> { ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0x43) -> { ControlFrame.U_FRAME_DISCONNECT }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0x0F) -> { ControlFrame.U_FRAME_DISCONNECT_MODE }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0x63) -> { ControlFrame.U_FRAME_UNNUMBERED_ACKNOWLEDGE }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0x87) -> { ControlFrame.U_FRAME_REJECT }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0x03) -> { ControlFrame.U_FRAME_UNNUMBERED_INFORMATION }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0xAF) -> { ControlFrame.U_FRAME_EXCHANGE_IDENTIFICATION }
            ByteUtils.compareMaskedByte(controlField,0xDF, 0xE3) -> { ControlFrame.U_FRAME_TEST }
            else -> {
                logger.error("Decoded an unknown AX.25 controlFrame ${StringUtils.byteToHex(controlField)}")
                ControlFrame.UNKNOWN_FRAME
            }
        }
    }

    private fun calculateReceiveSequenceNumber(): Int {
        val shiftedControlField = ByteUtils.shiftBitsRight(controlField, 5)
        return shiftedControlField.toInt()
    }

    private fun calculateSendSequenceNumber(): Int {
        val shiftedControlField = ByteUtils.shiftBitsRight(controlField, 1)
        val maskedControlField = ByteUtils.maskByte(shiftedControlField, 0x07)
        return maskedControlField.toInt()
    }

    private fun parseStringCallsign(callsign: String): Pair<ByteArray, Byte> {
        // Assume a default SSID of -0
        var callString = callsign
        var ssid =  0

        // Check if the callsign contains an SSID
        if (callsign.contains("-")) {
            callString = callsign.split("-")[0]
            val ssidString =  callsign.split("-")[1]
            ssid = ssidString.toInt()
        }

        val callsignBytes = StringUtils.convertStringToBytes(callString)
        val ssidByte = ssid.toByte()
        val shiftedSSID = ByteUtils.shiftBitsLeft(ssidByte, 1)
        val maskedSSID = ByteUtils.maskByte(shiftedSSID, 0x1D)
        val setbitsSSID = ByteUtils.setBits(maskedSSID, 0x60) // Set the reserved bits to 1
        return Pair(ByteUtils.shiftBitsLeft(callsignBytes, 1), setbitsSSID)
    }

    private fun parseReceiveSequenceNumber(sequenceNumberInt: Int): Byte {
        val sequenceNumberByte = sequenceNumberInt.toByte()
        return ByteUtils.shiftBitsLeft(sequenceNumberByte, 5)
    }

    private fun parseSendSequenceNumber(sequenceNumberInt: Int): Byte {
        val sequenceNumberByte = sequenceNumberInt.toByte()
        return ByteUtils.shiftBitsLeft(sequenceNumberByte, 1)
    }

}