package pakcatt.tnc.kiss

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

data class KissFrame(private val portAndCommand: Byte,
                     private val destCallsign: ByteArray,
                     private val destSSID: Byte,
                     private val sourceCallsign: ByteArray,
                     private val sourceSSID: Byte,
                     private val controlField: Byte,
                     private val protocolID: Byte,
                     private val payloadData: ByteArray) {

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

}