package pakcatt.network.packet.kiss

import org.slf4j.LoggerFactory
import pakcatt.util.ByteUtils
import pakcatt.util.StringUtils

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
        const val TYPE_I_FRAME = 0x00
        const val TYPE_S_FRAME_RECEIVE_READY = 0x01
        const val TYPE_S_FRAME_RECEIVE_NOT_READY = 0x05
        const val TYPE_S_FRAME_REJECT = 0x09
        const val TYPE_S_FRAME_SELECTIVE_REJECT = 0x0D
        const val TYPE_U_FRAME_SET_ASYNC_BALANCED_MODE = 0x6F
        const val TYPE_U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED = 0x2F
        const val TYPE_U_FRAME_DISCONNECT = 0x43
        const val TYPE_U_FRAME_DISCONNECT_MODE = 0x0F
        const val TYPE_U_FRAME_UNNUMBERED_ACKNOWLEDGE = 0x63
        const val TYPE_U_FRAME_REJECT = 0x87
        const val TYPE_U_FRAME_UNNUMBERED_INFORMATION = 0x03
        const val TYPE_U_FRAME_EXCHANGE_IDENTIFICATION = 0xAF
        const val TYPE_U_FRAME_TEST = 0xE3
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
    private val byteUtils = ByteUtils()
    private val stringUtils = StringUtils()
    private var portAndCommand: Byte = 0x00
    private var destCallsign: ByteArray = ByteArray(0)
    private var destSSID: Byte = 0x00
    private var sourceCallsign: ByteArray = ByteArray(0)
    private var sourceSSID: Byte = 0x00
    private var controlField: Byte = 0x00
    private var protocolID: Byte = 0xF0.toByte() // No layer 3
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
        // Set the source callsign ending in 1 to denote there is no repeater address
        val parsedCallsign = parseStringCallsign(sourceCallsign)
        this.sourceCallsign = parsedCallsign.first
        this.sourceSSID = byteUtils.setBits(parsedCallsign.second, 0x01)
    }

    fun setControlType(controlType: ControlFrame) {
        setControlFieldBits(controlType)
        setCommandBits(controlType)
    }

    fun setReceiveSequenceNumber(receiveSeq: Int) {
        val sequenceNumberByte = parseReceiveSequenceNumber(receiveSeq)
        this.controlField = byteUtils.setBits(controlField, sequenceNumberByte)
    }

    fun setSendSequenceNumber(sendSeq: Int) {
        val sequenceNumberByte = parseSendSequenceNumber(sendSeq)
        this.controlField = byteUtils.setBits(controlField, sequenceNumberByte)
    }

    fun setPollFinalBit(pollFinalBit: Boolean) {
        if (pollFinalBit) {
            controlField = byteUtils.setBits(controlField, 0x10)
        } else {
            controlField = byteUtils.maskByte(controlField, 0xDF)
        }
    }

    fun setPayloadMessage(message: String) {
        payloadData = stringUtils.convertStringToBytes(message)
    }

    fun packetData(): ByteArray {
        val packetSize = 5 + destCallsign.size + sourceCallsign.size + payloadData.size
        var kissPacket = ByteArray(packetSize)
        kissPacket[0] = portAndCommand
        byteUtils.insertIntoByteArray(destCallsign, kissPacket, 1)
        kissPacket[7] = destSSID
        byteUtils.insertIntoByteArray(sourceCallsign, kissPacket, 8)
        kissPacket[14] = sourceSSID
        kissPacket[15] = controlField
        kissPacket[16] = protocolID
        byteUtils.insertIntoByteArray(payloadData, kissPacket, 17)
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
        return stringUtils.convertBytesToString(payloadData)
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
        return byteUtils.compareMaskedByte(controlField,0x10, 0x10)
    }

    fun pollFinalBitString(): String {
        return when (pollFinalBit()) {
            true -> "1"
            false -> "0"
        }
    }

    override fun toString(): String {
        return "From: ${sourceCallsign()} to: ${destCallsign()} control: ${stringUtils.byteToHex(controlField())} " +
                "controlType: ${controlTypeString()} pollFinalBit: ${pollFinalBitString()} protocolID: ${stringUtils.byteToHex(protocolID())} " +
                "Receive Seq: ${receiveSequenceNumber()} Send Seq: ${sendSequenceNumber()}"
    }

    private fun constructCallsign(callsignByteArray: ByteArray, callsignSSID: Byte): String {
        val shiftedCallsign = byteUtils.shiftBitsRight(callsignByteArray, 1)
        val callsignString = stringUtils.convertBytesToString(shiftedCallsign)
        val trimmedCallsign = stringUtils.removeWhitespace(callsignString)
        val ssid = ssidFromSSIDByte(callsignSSID)
        return "${trimmedCallsign}-${ssid}"
    }

    private fun ssidFromSSIDByte(ssidByte: Byte): Int {
        val shiftedByte = byteUtils.shiftBitsRight(ssidByte, 1)
        return byteUtils.maskInt(shiftedByte.toInt(), 0x0F)
    }

    /**
     * Section 4.3 - Coding for Commands and Responses
     */
    private fun calculateControlFrame(): ControlFrame {
        return when {
            byteUtils.compareMaskedByte(controlField,0x01, TYPE_I_FRAME) -> { ControlFrame.I_FRAME }
            byteUtils.compareMaskedByte(controlField,0x0F, TYPE_S_FRAME_RECEIVE_READY) -> { ControlFrame.S_FRAME_RECEIVE_READY }
            byteUtils.compareMaskedByte(controlField,0x0F, TYPE_S_FRAME_RECEIVE_NOT_READY) -> { ControlFrame.S_FRAME_RECEIVE_NOT_READY }
            byteUtils.compareMaskedByte(controlField,0x0F, TYPE_S_FRAME_REJECT) -> { ControlFrame.S_FRAME_REJECT }
            byteUtils.compareMaskedByte(controlField,0x0F, TYPE_S_FRAME_SELECTIVE_REJECT) -> { ControlFrame.S_FRAME_SELECTIVE_REJECT }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_SET_ASYNC_BALANCED_MODE) -> { ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED) -> { ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_DISCONNECT) -> { ControlFrame.U_FRAME_DISCONNECT }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_DISCONNECT_MODE) -> { ControlFrame.U_FRAME_DISCONNECT_MODE }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_UNNUMBERED_ACKNOWLEDGE) -> { ControlFrame.U_FRAME_UNNUMBERED_ACKNOWLEDGE }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_REJECT) -> { ControlFrame.U_FRAME_REJECT }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_UNNUMBERED_INFORMATION) -> { ControlFrame.U_FRAME_UNNUMBERED_INFORMATION }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_EXCHANGE_IDENTIFICATION) -> { ControlFrame.U_FRAME_EXCHANGE_IDENTIFICATION }
            byteUtils.compareMaskedByte(controlField,0xEF, TYPE_U_FRAME_TEST) -> { ControlFrame.U_FRAME_TEST }
            else -> {
                logger.error("Decoded an unknown AX.25 controlFrame ${stringUtils.byteToHex(controlField)}")
                ControlFrame.UNKNOWN_FRAME
            }
        }
    }

    private fun calculateReceiveSequenceNumber(): Int {
        val shiftedControlField = byteUtils.shiftBitsRight(controlField, 5)
        return shiftedControlField.toInt()
    }

    private fun calculateSendSequenceNumber(): Int {
        val shiftedControlField = byteUtils.shiftBitsRight(controlField, 1)
        val maskedControlField = byteUtils.maskByte(shiftedControlField, 0x07)
        return maskedControlField.toInt()
    }

    private fun parseStringCallsign(callsign: String): Pair<ByteArray, Byte> {
        // Assume a default SSID of -0
        var callString = callsign
        var ssid = 0

        // Check if the callsign contains an SSID
        if (callsign.contains("-")) {
            callString = callsign.split("-")[0]
            val ssidString =  callsign.split("-")[1]
            ssid = ssidString.toInt()
        }

        // Pad out the callsign and format the call and ssid bits for AX.25
        callString = stringUtils.padWithSpaces(callString, 6)
        val callsignBytes = stringUtils.convertStringToBytes(callString)
        val ssidByte = ssid.toByte()
        val shiftedSSID = byteUtils.shiftBitsLeft(ssidByte, 1)
        val maskedSSID = byteUtils.maskByte(shiftedSSID, 0x1E)
        val setbitsSSID = byteUtils.setBits(maskedSSID, 0x60) // Set the reserved bits to 1
        return Pair(byteUtils.shiftBitsLeft(callsignBytes, 1), setbitsSSID)
    }

    private fun parseReceiveSequenceNumber(sequenceNumberInt: Int): Byte {
        val sequenceNumberByte = sequenceNumberInt.toByte()
        return byteUtils.shiftBitsLeft(sequenceNumberByte, 5)
    }

    private fun parseSendSequenceNumber(sequenceNumberInt: Int): Byte {
        val sequenceNumberByte = sequenceNumberInt.toByte()
        return byteUtils.shiftBitsLeft(sequenceNumberByte, 1)
    }

    /**
     * Section 4.3 - Coding for Commands and Responses
     */
    private fun setControlFieldBits(controlType: ControlFrame) {
        controlField = when(controlType) {
            ControlFrame.I_FRAME -> byteUtils.setBits(controlField, TYPE_I_FRAME)
            ControlFrame.S_FRAME_RECEIVE_READY -> byteUtils.setBits(controlField, TYPE_S_FRAME_RECEIVE_READY)
            ControlFrame.S_FRAME_RECEIVE_NOT_READY -> byteUtils.setBits(controlField, TYPE_S_FRAME_RECEIVE_NOT_READY)
            ControlFrame.S_FRAME_REJECT -> byteUtils.setBits(controlField, TYPE_S_FRAME_REJECT)
            ControlFrame.S_FRAME_SELECTIVE_REJECT -> byteUtils.setBits(controlField, TYPE_S_FRAME_SELECTIVE_REJECT)
            ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE -> byteUtils.setBits(controlField, TYPE_U_FRAME_SET_ASYNC_BALANCED_MODE)
            ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED -> byteUtils.setBits(controlField, TYPE_U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED)
            ControlFrame.U_FRAME_DISCONNECT -> byteUtils.setBits(controlField, TYPE_U_FRAME_DISCONNECT)
            ControlFrame.U_FRAME_DISCONNECT_MODE -> byteUtils.setBits(controlField, TYPE_U_FRAME_DISCONNECT_MODE)
            ControlFrame.U_FRAME_UNNUMBERED_ACKNOWLEDGE -> byteUtils.setBits(controlField, TYPE_U_FRAME_UNNUMBERED_ACKNOWLEDGE)
            ControlFrame.U_FRAME_REJECT -> byteUtils.setBits(controlField, TYPE_U_FRAME_REJECT)
            ControlFrame.U_FRAME_UNNUMBERED_INFORMATION -> byteUtils.setBits(controlField, TYPE_U_FRAME_UNNUMBERED_INFORMATION)
            ControlFrame.U_FRAME_EXCHANGE_IDENTIFICATION -> byteUtils.setBits(controlField, TYPE_U_FRAME_EXCHANGE_IDENTIFICATION)
            ControlFrame.U_FRAME_TEST -> byteUtils.setBits(controlField, TYPE_U_FRAME_TEST)
            ControlFrame.UNKNOWN_FRAME -> byteUtils.setBits(controlField, TYPE_I_FRAME) // Default to I frame
        }
    }

    /**
     * Section 6.1.2 - Command/Response Procedure
     * The control bit in the SSID bytes are set as either 1 or 0
     * to denote a command or a response packet. Below we set either
     * one of these bits to 1, assuming the other remains 0.
     */
    private fun setCommandBits(controlType: ControlFrame) {
        destSSID = when (controlType) {
            ControlFrame.I_FRAME -> byteUtils.setBits(destSSID, 0x80)
            ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE -> byteUtils.setBits(destSSID, 0x80)
            ControlFrame.U_FRAME_SET_ASYNC_BALANCED_MODE_EXTENDED -> byteUtils.setBits(destSSID, 0x80)
            ControlFrame.U_FRAME_DISCONNECT -> byteUtils.setBits(destSSID, 0x80)
            ControlFrame.U_FRAME_DISCONNECT_MODE -> byteUtils.setBits(destSSID, 0x80)
            ControlFrame.U_FRAME_UNNUMBERED_INFORMATION -> byteUtils.setBits(destSSID, 0x80)
            ControlFrame.U_FRAME_EXCHANGE_IDENTIFICATION -> byteUtils.setBits(destSSID, 0x80)
            ControlFrame.U_FRAME_TEST -> byteUtils.setBits(destSSID, 0x80)
            else -> destSSID
        }

        sourceSSID = when (controlType) {
            ControlFrame.S_FRAME_RECEIVE_READY -> byteUtils.setBits(sourceSSID, 0x80)
            ControlFrame.S_FRAME_RECEIVE_NOT_READY -> byteUtils.setBits(sourceSSID, 0x80)
            ControlFrame.S_FRAME_REJECT -> byteUtils.setBits(sourceSSID, 0x80)
            ControlFrame.S_FRAME_SELECTIVE_REJECT -> byteUtils.setBits(sourceSSID, 0x80)
            ControlFrame.U_FRAME_UNNUMBERED_ACKNOWLEDGE -> byteUtils.setBits(sourceSSID, 0x80)
            ControlFrame.U_FRAME_REJECT -> byteUtils.setBits(sourceSSID, 0x80)
            ControlFrame.UNKNOWN_FRAME -> byteUtils.setBits(sourceSSID, 0x80)
            else -> sourceSSID
        }
    }

}