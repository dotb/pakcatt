package pakcatt.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@Component
class StringUtils {

    companion object {
        const val CRLF = "\r\n"
        const val LFCR = "\n\r"
        const val CR = "\r"
        const val LF = "\n"
    }

    @Autowired
    private var defaultEndOfLine: String = LF

    val EOL: String = defaultEndOfLine
    private val dateFormatter = SimpleDateFormat("dd MMM HH:mm")

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

    fun stringToHex(string: String): String {
        var stringBuilder = StringBuilder()
        for (char in string) {
            val hexChar = byteToHex(char.toByte())
            stringBuilder.append(hexChar)
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

    fun convertByteToString(byte: Byte): String {
        val byteArrayOfOne = ByteArray(1)
        byteArrayOfOne[0] = byte
        return convertBytesToString(byteArrayOfOne)
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
        return removeEOLChars(input, "")
    }

    fun removeEOLChars(input: String, substitutionString: String): String {
        var returnedString = input.replace("\r",substitutionString)
        returnedString = returnedString.replace("\n", substitutionString)
        return returnedString
    }

    /**
     * Remove any SSID number and make the callsign uppercase
     */
    fun formatCallsignRemoveSSID(callsign: String): String {
        val callsignOnly = when (callsign.contains("-")) {
            true -> callsign.split("-")[0]
            false -> callsign
        }
        return callsignOnly.toUpperCase()
    }

    /**
     * Format a callsign, ensuring it has at least
     * the default SSID
     */
    fun formatCallsignEnsureSSID(callsign: String): String {
        return when (callsign.contains("-")) {
            true -> callsign.toUpperCase()
            false -> "$callsign-0".toUpperCase()
        }
    }

    /**
     * Parse an input array of bytes and fix any End Of Line (EOL)
     * characters, replacing them with a specified EOL sequence.
     */
    fun fixEndOfLineCharacters(inputString: String, newEOLString: String): String {
        var fixedString = inputString

        // Swap CRLF with LF
        fixedString = fixedString.replace("${CR}${LF}", LF)
        // Swap LFCR with LF
        fixedString = fixedString.replace("${LF}${CR}", LF)
        // Swap CR with LF
        fixedString = fixedString.replace(CR, LF)
        // All EOL sqeuences should be reduced to LF, now swap it with the designated new EOL string
        fixedString = fixedString.replace(LF, newEOLString)

        return fixedString.toString()
    }

    /**
     * Create a string of a fixed size, padding with space
     * on the end of the string if required.
     */
    fun fixedSizeString(inputString: String, fixedSize: Int): String {
        var returnedString = inputString
        for (index in returnedString.length..fixedSize) {
            returnedString += " "
        }
        return trimmedString(returnedString, fixedSize)
    }

    fun trimmedString(inputString: String, maxLength: Int): String {
        return inputString.substring(0, min(maxLength, inputString.length))
    }

    fun formattedDate(date: Date): String {
        return dateFormatter.format(date.time)
    }

    fun shortenString(string: String, maxLength: Int, withEllipsis: Boolean): String {
        val length = maxLength.coerceAtMost(string.length)
        return if (length < string.length && withEllipsis) {
            "${string.substring(0 until length)}..."
        } else {
            string.substring(0 until length)
        }
    }

}