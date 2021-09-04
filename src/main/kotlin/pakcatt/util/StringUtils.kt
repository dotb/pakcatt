package pakcatt.util

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

@Component
class StringUtils {

    @Autowired
    private var defaultEndOfLine: String = "\r\n"

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
     * characters, replacing single instances of \r or \n
     * with the configured standard EOL character sequence.
     */
    fun fixEndOfLineCharacters(inputString: String): String {
        val lineFeed = '\r'
        val carriageReturn = '\n'
        var fixedString = StringBuilder()

        // Run through the remaining bytes and look for missing EOL characters
        for ((index, currentChar) in inputString.withIndex()) {
            val previousChar = when {
                index - 1 >= 0 -> inputString[index - 1]
                else -> 0
            }
            val nextChar = when {
                index + 1 < inputString.length -> inputString[index + 1]
                else -> 0
            }

            // Change any instances of CR or LF with the configured EOL sequence
            when {
                currentChar == lineFeed && previousChar != carriageReturn && nextChar != carriageReturn -> {
                    fixedString.append(EOL)
                }
                currentChar == carriageReturn && previousChar != lineFeed && nextChar != lineFeed -> {
                    fixedString.append(EOL)
                }
                else -> fixedString.append(currentChar)
            }
        }

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