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
        return  callsignOnly.toUpperCase()
    }

    /**
     * Parse an input array of bytes and fix any End Of Line (EOL)
     * characters, replacing single instances of \r or \n
     * with both \n\r.
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

            // Fix any single instances of CR or LF with both
            when {
                currentChar == lineFeed && previousChar != carriageReturn && nextChar != carriageReturn -> {
                    fixedString.append(lineFeed)
                    fixedString.append(carriageReturn)

                }
                currentChar == carriageReturn && previousChar != lineFeed && nextChar != lineFeed -> {
                    fixedString.append(lineFeed)
                    fixedString.append(carriageReturn)
                }
                else -> fixedString.append(currentChar)
            }
        }

        return fixedString.toString()
    }

}