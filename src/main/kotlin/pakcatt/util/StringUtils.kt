package pakcatt.util

import org.springframework.stereotype.Component

@Component
class StringUtils {

    fun byteToHex(byte: Byte): String {
        return String.format("%02x", byte)
    }

    fun intToHex(int: Int): String {
        return String.format("%02x", int)
    }

    fun byteArrayToHex(byteArray: ByteArray): String {
        var stringBuilder = StringBuilder()

        for (byte in byteArray) {
            val byteAsHex = byteToHex(byte)
            stringBuilder.append(byteAsHex)
            stringBuilder.append(" ")
        }
        return stringBuilder.toString()
    }

    fun removeWhitespace(string: String): String {
        return string.replace(" ", "")
    }

    fun convertBytesToString(byteArray: ByteArray): String {
        return String(byteArray, Charsets.US_ASCII)
    }

    fun convertStringToBytes(string: String): ByteArray {
        return string.toByteArray(Charsets.US_ASCII)
    }

    fun padWithSpaces(string: String, totalLength: Int): String {
        var paddedString = StringBuilder(string)
        for (i in string.length + 1 .. totalLength) {
            paddedString.append(" ")
        }
        return paddedString.toString()
    }

    /**
     * Remove any end-of-line characters from a string
     */
    fun removeEOLChars(input: String): String {
        var chompedString = input.replace("\r","")
        chompedString = chompedString.replace("\n", "")
        return chompedString
    }

    /**
     * Remove any SSID number and make the callsign uppercase
     */
    fun formatCallsignRemoveSSID(callsign: String): String {
        val callsignOnly = when (callsign.contains("-")) {
            true -> callsign.split("-")[0]
            false -> callsign
        }
        return  callsignOnly.toUpperCase()
    }

}