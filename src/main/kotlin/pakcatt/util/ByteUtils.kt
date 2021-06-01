package pakcatt.util

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.experimental.and
import kotlin.experimental.or

@Component
class ByteUtils {

    private val logger = LoggerFactory.getLogger(ByteUtils::class.java)

    /**
     * Convert from a Byte to an Int, retaining the bit formation
     * of the Byte.
     *
     * From the Kotlin docs: https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-byte/to-int.html
     * The resulting Int value represents the same numerical value as this Byte.
     * The least significant 8 bits of the resulting Int value are the same as the bits of this Byte value,
     * whereas the most significant 24 bits are filled with the sign bit of this value.
     *
     * This means we might end up with 1's in the most significant bits of the Int, so we
     * mask them out just in case, to ensure we retain the same bit pattern that was in the original
     * byte.
     */
    fun byteToInt(byte: Byte): Int {
        val convertedInt = byte.toInt()
        return maskInt(convertedInt, 0x000000FF)
    }

    /**
     * Convert from an Int to a Byte, retaining the integrity of
     * the bit formation from the Int.
     */
    fun intToByte(int: Int): Byte {
        return int.toByte()
    }

    /**
     * Shift the bits in an array of bytes to the left
     */
    fun shiftBitsLeft(byteArray: ByteArray, places: Int): ByteArray {
        var shiftedArray = ByteArray(byteArray.size)
        for ((index, byte) in byteArray.withIndex()) {
            val shiftedByte = shiftBitsLeft(byte, places)
            shiftedArray[index] = shiftedByte
        }
        return shiftedArray
    }

    fun shiftBitsRight(byteArray: ByteArray, places: Int): ByteArray {
        var shiftedArray = ByteArray(byteArray.size)
        for ((index, byte) in byteArray.withIndex()) {
            val shiftedByte = shiftBitsRight(byte, places)
            shiftedArray[index] = shiftedByte
        }
        return shiftedArray
    }

    /*
    * When we convert a byte to an integer type additional 1's are added
    * to the high bytes. We use a mask to remove these and ensure only
    * the original byte is manipulated.
     */
    fun shiftBitsLeft(byte: Byte, places: Int): Byte {
        val intVal = byte.toInt()
        val maskedInt = maskInt(intVal, 0x000000FF)
        val shiftedInt = maskedInt shl places
        return intToByte(shiftedInt)
    }

    fun shiftBitsRight(byte: Byte, places: Int): Byte {
        val intVal = byte.toInt()
        val maskedInt = maskInt(intVal, 0x000000FF)
        val shiftedInt = maskedInt shr places
        return intToByte(shiftedInt)
    }

    fun shiftBitsRight(int: Int, places: Int): Int {
        val shiftedInt = int shr places
        return maskInt(shiftedInt, 0x000000FF)
    }

    fun maskByte(byte: Byte, mask: Int): Byte {
        val maskAsByte = intToByte(mask)
        return byte.and(maskAsByte)
    }

    fun maskInt(int: Int, mask: Int):Int {
        return int.and(mask)
    }

    fun setBits(byte: Byte, mask: Int): Byte {
        return setBits(byte, mask.toByte())
    }

    fun setBits(byte: Byte, mask: Byte): Byte {
        return byte.or(mask)
    }

    fun compareMaskedByte(sourceByte: Byte, mask: Int, comparisonByte: Int): Boolean {
        val maskedSourceByte = maskByte(sourceByte, mask)
        val maskedSourceInt = byteToInt(maskedSourceByte)
        return maskedSourceInt.compareTo(comparisonByte) == 0
    }

    fun insertIntoByteArray(source: Byte, destination: ByteArray, index: Int): Int {
        return if (destination.size > index) {
            destination[index] = source
            index + 1
        } else {
            index
        }
    }

    fun insertIntoByteArray(source: ByteArray, destination: ByteArray, startIndex: Int): Int {
        var index = startIndex
        for (byte in source) {
            index = insertIntoByteArray(byte, destination, index)
        }
        return index
    }

    /**
     * Parse an input array of bytes and fix any End Of Line (EOL)
     * characters, replacing single instances of \r or \n
     * with both \r\n.
     */
    fun fixEndOfLineCharacters(inputBytes: ByteArray): ByteArray {
        val lineFeed = intToByte(12)
        val carriageReturn = intToByte(13)
        var updatedArray = ArrayList<Byte>()

        // Run through the remaining bytes and look for missing EOL characters
        for ((index, currentByte) in inputBytes.withIndex()) {
            val previousByte = when {
                index - 1 >= 0 -> inputBytes[index - 1]
                else -> 0
            }
            val nextByte = when {
                index + 1 < inputBytes.size -> inputBytes[index + 1]
                else -> 0
            }

            // Fix any single instances of CR or LF with both
            when {
                currentByte == lineFeed && previousByte != carriageReturn && nextByte != carriageReturn -> {
                    updatedArray.add(carriageReturn)
                    updatedArray.add(lineFeed)
                }
                currentByte == carriageReturn && previousByte != lineFeed && nextByte != lineFeed -> {
                    updatedArray.add(carriageReturn)
                    updatedArray.add(lineFeed)
                }
                else -> updatedArray.add(currentByte)
            }
        }

        return updatedArray.toByteArray()
    }


}