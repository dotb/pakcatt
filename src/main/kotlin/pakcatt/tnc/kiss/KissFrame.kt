package pakcatt.tnc.kiss

import org.slf4j.LoggerFactory
import pakcatt.util.Utils
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

    enum class ControlType {
        I_FRAME, S_FRAME, U_FRAME, UNKNOWN_FRAME
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
        return convertBytesToString(payloadData)
    }

    fun controlType(): ControlType {
        return calculateControlType()
    }

    fun controlTypeString(): String {
        return when (calculateControlType()) {
            ControlType.I_FRAME -> "I"
            ControlType.S_FRAME -> "S"
            ControlType.U_FRAME -> "U"
            ControlType.UNKNOWN_FRAME -> "?"
        }
    }

    private fun constructCallsign(callsignByteArray: ByteArray, callsignSSID: Byte): String {
        val shiftedCallsign = shiftBitsLeft(callsignByteArray)
        val callsignString = convertBytesToString(shiftedCallsign)
        val trimmedCallsign = removeWhitespace(callsignString)
        val ssid = ssidFromSSIDByte(callsignSSID)
        return "${trimmedCallsign}-${ssid}"
    }

    private fun ssidFromSSIDByte(ssidByte: Byte): Int {
        val ssidMask = 0x1E
        val maskedSSID = ssidByte.and(ssidMask.toByte())
        return maskedSSID.toInt() shr 1
    }

    private fun convertBytesToString(byteArray: ByteArray): String {
        return String(byteArray, Charsets.US_ASCII)
    }

    private fun shiftBitsLeft(byteArray: ByteArray): ByteArray {
        /* When we convert a byte to an integer type additional 1's are added
           to the high bytes. We use a mask to remove these and ensure only
           the original byte is manipulated. */
        val intMask = 0x000000FF
        var shiftedArray = ByteArray(byteArray.size)
        for ((index, byte) in byteArray.withIndex()) {
            val intVal = byte.toInt()
            val maskedInt = intVal.and(intMask)
            val shiftedInt = maskedInt shr 1
            val shiftedByte = shiftedInt.toByte()
            shiftedArray[index] = shiftedByte
            logger.trace("byte: ${Utils.byteToHex(byte)} shiftedByte: ${Utils.byteToHex(shiftedByte)} intVal: ${Utils.intToHex(intVal)} maskedInt: ${Utils.intToHex(maskedInt)} shiftedInt: ${Utils.intToHex(shiftedInt)}")
        }
        return shiftedArray
    }

    private fun removeWhitespace(string: String): String {
        return string.replace(" ", "")
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
    private fun calculateControlType(): ControlType {
        val iFrameHash = 0x00000001
        val suFrameHash = 0x00000003
        var intControlByte = controlField.toInt()
        return if (0x00 == intControlByte.and(iFrameHash)) {
            ControlType.I_FRAME
        } else if (0x01 == intControlByte.and(suFrameHash)) {
            ControlType.S_FRAME
        } else if (0x03 == intControlByte.and(suFrameHash)) {
            ControlType.U_FRAME
        } else {
            ControlType.UNKNOWN_FRAME
        }
    }

}