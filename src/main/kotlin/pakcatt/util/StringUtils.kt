package pakcatt.util

class StringUtils {

companion object {
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

}

}