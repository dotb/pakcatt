package pakcatt.util

class Utils {

companion object {
    fun byteToHex(byte: Byte): String {
        return String.format("%02x", byte)
    }

    fun intToHex(int: Int): String {
        return String.format("%02x", int)
    }

    fun byteArrayToHex(byteArray: ByteArray): String {
        var hexString = ""
        for (byte in byteArray) {
            val byteAsHex = byteToHex(byte)
            hexString += " $hexString"
        }
        return hexString
    }
}

}