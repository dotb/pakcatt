package pakcatt.network.radio.kiss.model

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
Although the documentation seems to indicate it could include more repeaters, but this
is being phased out, and I've not seen it in the wild.
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

Protocol ID        1 byte (always 0xf0 - no layer 3 protocol) (AX.25). Only used in I and UI frames.
Information Field  1-256 bytes (payload data) (AX.25)

Frame End (FEND)   1 byte (0xc0) (KISS)
 */

enum class ProtocolID(val id: Int) {
    NO_LAYER_3(0xF0)
}

enum class ControlField(val mask: Int, val bitPattern: Int) {
    INFORMATION_8(0x11, 0x00),
    INFORMATION_8_P(0x11,0x10),
    INFORMATION_128(0x0101,0x0000),
    INFORMATION_128_P(0x0101,0x0100),

    S_8_RECEIVE_READY(0x1F,0x01), S_8_RECEIVE_NOT_READY(0x1F,0x05), S_8_REJECT(0x1F,0x09), S_8_SELECTIVE_REJECT(0x1F,0x0D),
    S_8_RECEIVE_READY_P(0x1F,0x11), S_8_RECEIVE_NOT_READY_P(0x1F,0x15), S_8_REJECT_P(0x1F,0x019), S_8_SELECTIVE_REJECT_P(0x1F,0x1D),
    S_128_RECEIVE_READY(0x01FF,0x0001), S_128_RECEIVE_NOT_READY(0x01FF,0x0005), S_128_REJECT(0x01FF,0x0009), S_128_SELECTIVE_REJECT(0x01FF,0x000D),
    S_128_RECEIVE_READY_P(0x01FF,0x0101), S_128_RECEIVE_NOT_READY_P(0x01FF,0x0105), S_128_REJECT_P(0x01FF,0x0109), S_128_SELECTIVE_REJECT_P(0x01FF,0x010D),

    U_SET_ASYNC_BALANCED_MODE_EXTENDED(0xFF,0x6F), U_SET_ASYNC_BALANCED_MODE(0xFF,0x2F), U_DISCONNECT(0xFF,0x43), U_DISCONNECT_MODE(0xFF,0x0F), U_UNNUMBERED_ACKNOWLEDGE(0xFF,0x63), U_REJECT(0xFF,0x87), U_UNNUMBERED_INFORMATION(0xFF,0x03), U_EXCHANGE_IDENTIFICATION(0xFF,0xAF), U_TEST(0xFF,0xE3),
    U_SET_ASYNC_BALANCED_MODE_EXTENDED_P(0xFF,0x7F), U_SET_ASYNC_BALANCED_MODE_P(0xFF,0x3F), U_DISCONNECT_P(0xFF,0x53), U_DISCONNECT_MODE_P(0xFF,0x1F), U_UNNUMBERED_ACKNOWLEDGE_P(0xFF,0x73), U_REJECT_P(0xFF,0x97), U_UNNUMBERED_INFORMATION_P(0xFF,0x13), U_EXCHANGE_IDENTIFICATION_P(0xFF,0xBF), U_TEST_P(0xFF,0xF3),
    UNKNOWN_Field(0xFF,0xFF)
}

abstract class KissFrame() {

    protected val byteUtils = ByteUtils()
    protected val stringUtils = StringUtils()
    protected var portAndCommand: Byte = byteUtils.intToByte(0x00)
    protected var destCallsign: ByteArray = ByteArray(0)
    protected var destSSID: Byte = byteUtils.intToByte(0x00)
    protected var sourceCallsign: ByteArray = ByteArray(0)
    protected var sourceSSID: Byte = byteUtils.intToByte(0x00)
    protected var repeaterCallsignOne: ByteArray = ByteArray(0)
    protected var repeaterSSIDOne: Byte = byteUtils.intToByte(0x00)
    protected var repeaterCallsignTwo: ByteArray = ByteArray(0)
    protected var repeaterSSIDTwo: Byte = byteUtils.intToByte(0x00)
    protected var protocolID: Byte = byteUtils.intToByte(0x00)
    protected var payloadData: ByteArray = ByteArray(0)
    var lastDeliveryAttemptTimeStamp: Long = 0
    var deliveryAttempts = 0

    companion object {
        const val FRAME_END = -64
        const val SIZE_MIN = 15
        const val SIZE_HEADERS = 18
    }

    open fun populateFromFrameData(frameByteData: ByteArray) {
        // Set defaults for optional fields
        this.repeaterCallsignOne = ByteArray(0)
        this.repeaterCallsignTwo = ByteArray(0)
        this.repeaterSSIDOne = byteUtils.intToByte(0x00)
        this.repeaterSSIDTwo = byteUtils.intToByte(0x00)
        this.protocolID = byteUtils.intToByte(0x00)
        this.payloadData = ByteArray(0)

        // Mandatory fields
        var nextIndex = 0
        this.portAndCommand = frameByteData[nextIndex]
        nextIndex++
        this.destCallsign = frameByteData.copyOfRange(nextIndex, nextIndex + 6)
        nextIndex += 6
        this.destSSID = frameByteData[nextIndex]
        nextIndex++
        this.sourceCallsign = frameByteData.copyOfRange(nextIndex, nextIndex + 6)
        nextIndex += 6
        this.sourceSSID = frameByteData[nextIndex]
        nextIndex++

        // Is there repeater information to decode?
        // Last bit in sourceSSID set to 0 - yes, 1 - no
        if (byteUtils.maskByte(sourceSSID, 0x01) == byteUtils.intToByte(0x00)) {
            // At least one repeater address is expected
            this.repeaterCallsignOne = frameByteData.copyOfRange(nextIndex, nextIndex + 6)
            nextIndex += 6
            this.repeaterSSIDOne = frameByteData[nextIndex]
            nextIndex++
            if (byteUtils.maskByte(repeaterSSIDOne, 0x01) == byteUtils.intToByte(0x00)) {
                this.repeaterCallsignTwo = frameByteData.copyOfRange(nextIndex, nextIndex + 6)
                nextIndex += 6
                this.repeaterSSIDTwo = frameByteData[nextIndex]
                nextIndex++
            }
        }

        // Call to the standard or extended frame classes to handle the control field
        nextIndex = setControlFieldFromFrameData(frameByteData, nextIndex)

        // The protocol ID is optional
        if (frameByteData.size > nextIndex) {
            this.protocolID = frameByteData[nextIndex]
            nextIndex++
        }

        // Information / payload data is optional
        if (frameByteData.size > nextIndex) {
            this.payloadData = frameByteData.copyOfRange(nextIndex, frameByteData.size)
        }
    }

    open fun packetData(): ByteArray {
        val controlField = controlBits()

        var packetSize = controlField.size + destCallsign.size + sourceCallsign.size +
                repeaterCallsignOne.size + repeaterCallsignTwo.size + payloadData.size

        // Add space any repeater SSIDs
        if (repeaterCallsignOne.isNotEmpty()) {
            packetSize++
        }
        if (repeaterCallsignTwo.isNotEmpty()) {
            packetSize++
        }

        // Section 3.4. PID Field. The PID field is only sent on I and UI Frames
        packetSize += if (arrayOf(
                ControlField.INFORMATION_8,
                ControlField.INFORMATION_8_P,
                ControlField.INFORMATION_128,
                ControlField.INFORMATION_128_P,
                ControlField.U_UNNUMBERED_INFORMATION,
                ControlField.U_UNNUMBERED_INFORMATION_P)
                .contains(calculateControlFrame())) {
            4
        } else {
            3
        }

        var nextIndex = 0
        var kissPacket = ByteArray(packetSize)
        nextIndex = byteUtils.insertIntoByteArray(portAndCommand, kissPacket, nextIndex)
        nextIndex = byteUtils.insertIntoByteArray(destCallsign, kissPacket, nextIndex)
        nextIndex = byteUtils.insertIntoByteArray(destSSID, kissPacket, nextIndex)
        nextIndex = byteUtils.insertIntoByteArray(sourceCallsign, kissPacket, nextIndex)
        nextIndex = byteUtils.insertIntoByteArray(sourceSSID, kissPacket, nextIndex)
        nextIndex = byteUtils.insertIntoByteArray(repeaterCallsignOne, kissPacket, nextIndex)
        if (repeaterCallsignOne.isNotEmpty()) {
            nextIndex = byteUtils.insertIntoByteArray(repeaterSSIDOne, kissPacket, nextIndex)
        }
        nextIndex = byteUtils.insertIntoByteArray(repeaterCallsignTwo, kissPacket, nextIndex)
        if (repeaterCallsignTwo.isNotEmpty()) {
            nextIndex = byteUtils.insertIntoByteArray(repeaterSSIDTwo, kissPacket, nextIndex)
        }
        nextIndex = byteUtils.insertIntoByteArray(controlField, kissPacket, nextIndex)
        nextIndex = byteUtils.insertIntoByteArray(protocolID, kissPacket, nextIndex) // Optional
        byteUtils.insertIntoByteArray(payloadData, kissPacket, nextIndex) // Optional
        return kissPacket
    }

    fun setDestCallsign(destCallsign: String) {
        val parsedCallsign = parseStringCallsign(destCallsign)
        this.destCallsign = parsedCallsign.first
        this.destSSID = parsedCallsign.second
    }

    fun setSourceCallsign(sourceCallsign: String) {
        // Set the source callsign ending in 1 to denote there is no repeater address
        val uppercaseCallsign = sourceCallsign.toUpperCase()
        val parsedCallsign = parseStringCallsign(uppercaseCallsign)
        this.sourceCallsign = parsedCallsign.first
        this.sourceSSID = byteUtils.setBits(parsedCallsign.second, 0x01)
    }

    fun setControlField(controlType: ControlField, receiveSeq: Int = 0, sendSeq: Int = 0) {
        setControlFrame(controlType, receiveSeq, sendSeq)
        setCommandBits(controlType)
    }

    fun setReceiveSequenceNumberIfRequired(receiveSeq: Int) {
        if (requiresReceiveSequenceNumber()) {
            setControlField(controlField(), receiveSeq, sendSequenceNumber())
        }
    }

    fun setSendSequenceNumber(sendSeq: Int) {
        setControlField(controlField(), receiveSequenceNumber(), sendSeq)
    }

    fun setPayloadMessage(message: String) {
        payloadData = stringUtils.convertStringToBytes(message)
    }

    fun sourceCallsign(): String {
        return constructCallsign(sourceCallsign, sourceSSID)
    }

    fun destCallsign(): String {
        return constructCallsign(destCallsign, destSSID)
    }

    fun repeaterCallsignOne(): String {
        return constructCallsign(repeaterCallsignOne, repeaterSSIDOne)
    }

    fun repeaterCallsignTwo(): String {
        return constructCallsign(repeaterCallsignTwo, repeaterSSIDTwo)
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

    fun controlField(): ControlField {
        return calculateControlFrame()
    }

    fun receiveSequenceNumber(): Int {
        return calculateReceiveSequenceNumber()
    }

    fun sendSequenceNumber(): Int {
        return calculateSendSequenceNumber()
    }

    fun controlTypeString(): String {
        return calculateControlFrame().toString()
    }

    /**
     * Returns true if this frame needs to be acknowledged
     * be the node receiving it. I.e. It's send sequence number
     * needs to be acknowledged.
     */
    fun requiresSendSequenceNumber(): Boolean {
        return arrayListOf(
            ControlField.INFORMATION_8,
            ControlField.INFORMATION_8_P,
            ControlField.INFORMATION_128,
            ControlField.INFORMATION_128_P
            ).contains(controlField())
    }

    fun requiresReceiveSequenceNumber(): Boolean {
        return listOf(
            ControlField.INFORMATION_8, ControlField.INFORMATION_8_P,
            ControlField.INFORMATION_128, ControlField.INFORMATION_128_P,
            ControlField.S_8_RECEIVE_READY, ControlField.S_8_RECEIVE_READY_P,
            ControlField.S_8_RECEIVE_NOT_READY, ControlField.S_8_RECEIVE_NOT_READY_P,
            ControlField.S_8_REJECT, ControlField.S_8_REJECT_P,
            ControlField.S_8_SELECTIVE_REJECT, ControlField.S_8_SELECTIVE_REJECT_P,
            ControlField.S_128_RECEIVE_NOT_READY, ControlField.S_128_RECEIVE_READY_P,
            ControlField.S_128_RECEIVE_READY, ControlField.S_128_RECEIVE_READY_P,
            ControlField.S_128_REJECT, ControlField.S_128_REJECT_P,
            ControlField.S_128_SELECTIVE_REJECT, ControlField.S_128_SELECTIVE_REJECT_P
        ).contains(controlField())
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        if (listOf(
                ControlField.S_8_RECEIVE_READY,
                ControlField.S_8_RECEIVE_READY_P,
                ControlField.S_8_RECEIVE_NOT_READY,
                ControlField.S_8_RECEIVE_NOT_READY_P,
                ControlField.S_8_REJECT,
                ControlField.S_8_REJECT_P,
                ControlField.S_8_SELECTIVE_REJECT,
                ControlField.S_8_SELECTIVE_REJECT_P,
                ControlField.S_128_RECEIVE_READY,
                ControlField.S_128_RECEIVE_READY_P,
                ControlField.S_128_RECEIVE_NOT_READY,
                ControlField.S_128_RECEIVE_NOT_READY_P,
                ControlField.S_128_REJECT,
                ControlField.S_128_REJECT_P,
                ControlField.S_128_SELECTIVE_REJECT,
                ControlField.S_128_SELECTIVE_REJECT_P
            ).contains(calculateControlFrame())) {
            stringBuilder.append("Receive Seq: ${receiveSequenceNumber()} ")
        }

        if (listOf(
                ControlField.INFORMATION_8, ControlField.INFORMATION_8_P,
                ControlField.INFORMATION_128, ControlField.INFORMATION_128_P
            ).contains(calculateControlFrame())) {
            stringBuilder.append("Receive Seq: ${receiveSequenceNumber()} Send Seq: ${sendSequenceNumber()} Delivery Attempt: $deliveryAttempts protocolID: ${stringUtils.byteToHex(protocolID())} ")
        }

        stringBuilder.append("From: ${sourceCallsign()} to: ${destCallsign()} controlType: ")

        if (repeaterCallsignOne.isNotEmpty()) {
            stringBuilder.append("Via1: ${repeaterCallsignOne()} ")
        }

        if (repeaterCallsignTwo.isNotEmpty()) {
            stringBuilder.append("Via2: ${repeaterCallsignTwo()} ")
        }

        stringBuilder.append("controlType: ${controlTypeString()} ")

        if (payloadData.isNotEmpty()) {
            stringBuilder.append("Payload: ${payloadDataString()}")
        }

        return stringBuilder.toString()
    }

    private fun constructCallsign(callsignByteArray: ByteArray, callsignSSID: Byte): String {
        return if (callsignByteArray.isNotEmpty()) {
            val shiftedCallsign = byteUtils.shiftBitsRight(callsignByteArray, 1)
            val callsignString = stringUtils.convertBytesToString(shiftedCallsign)
            val trimmedCallsign = stringUtils.removeWhitespace(callsignString)
            val ssid = ssidFromSSIDByte(callsignSSID)
            "${trimmedCallsign}-${ssid}"
        } else {
            ""
        }
    }

    private fun ssidFromSSIDByte(ssidByte: Byte): Int {
        val shiftedByte = byteUtils.shiftBitsRight(ssidByte, 1)
        return byteUtils.maskInt(shiftedByte.toInt(), 0x0F)
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

    /**
     * Section 6.1.2 - Command/Response Procedure
     * The control bit in the SSID bytes are set as either 1 or 0
     * to denote a command or a response packet. Below we set either
     * one of these bits to 1, assuming the other remains 0.
     */
    private fun setCommandBits(controlType: ControlField) {
        destSSID = when (controlType) {
            ControlField.INFORMATION_8 -> byteUtils.setBits(destSSID, 0x80)
            ControlField.INFORMATION_8_P -> byteUtils.setBits(destSSID, 0x80)
            ControlField.INFORMATION_128 -> byteUtils.setBits(destSSID, 0x80)
            ControlField.INFORMATION_128_P -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_SET_ASYNC_BALANCED_MODE -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_SET_ASYNC_BALANCED_MODE_EXTENDED -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_DISCONNECT -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_DISCONNECT_MODE -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_UNNUMBERED_INFORMATION -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_EXCHANGE_IDENTIFICATION -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_TEST -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_SET_ASYNC_BALANCED_MODE_P -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_SET_ASYNC_BALANCED_MODE_EXTENDED_P -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_DISCONNECT_P -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_DISCONNECT_MODE_P -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_UNNUMBERED_INFORMATION_P -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_EXCHANGE_IDENTIFICATION_P -> byteUtils.setBits(destSSID, 0x80)
            ControlField.U_TEST_P -> byteUtils.setBits(destSSID, 0x80)
            else -> destSSID
        }

        sourceSSID = when (controlType) {
            ControlField.S_8_RECEIVE_READY -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_8_RECEIVE_NOT_READY -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_8_REJECT -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_8_SELECTIVE_REJECT -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_8_RECEIVE_READY_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_8_RECEIVE_NOT_READY_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_8_REJECT_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_8_SELECTIVE_REJECT_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_128_RECEIVE_READY -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_128_RECEIVE_NOT_READY -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_128_REJECT -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_128_SELECTIVE_REJECT -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_128_RECEIVE_READY_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_128_RECEIVE_NOT_READY_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_128_REJECT_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.S_128_SELECTIVE_REJECT_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.U_UNNUMBERED_ACKNOWLEDGE -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.U_REJECT -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.U_UNNUMBERED_ACKNOWLEDGE_P -> byteUtils.setBits(sourceSSID, 0x80)
            ControlField.U_REJECT_P -> byteUtils.setBits(sourceSSID, 0x80)
            else -> sourceSSID
        }
    }

    /**
     * 4.3. Control-Field Coding for Commands and Responses
     * The following Control Field methods are implemented by a child class
     * so that both 1 and 2 byte Control Field parameters
     * can be handled to support Extended mode.
     */
    abstract fun controlBits(): ByteArray

    abstract fun pollFinalBit(): Boolean

    protected abstract fun setControlFieldFromFrameData(frameByteData: ByteArray, nextIndex: Int): Int

    protected abstract fun setControlFrame(controlType: ControlField, receiveSeq: Int, sendSeq: Int)

    protected abstract fun calculateControlFrame(): ControlField

    protected abstract fun calculateReceiveSequenceNumber(): Int

    protected abstract fun calculateSendSequenceNumber(): Int

}