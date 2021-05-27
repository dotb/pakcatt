package pakcatt.util

import org.slf4j.LoggerFactory
import pakcatt.tnc.kiss.KissFrame
import kotlin.experimental.and

class ByteUtils {

    companion object {

        private val logger = LoggerFactory.getLogger(KissFrame::class.java)

        /**
         * Shift the bits in an array of bytes to the left
         */
        fun shiftBitsLeft(byteArray: ByteArray, places: Int): ByteArray {
            var shiftedArray = ByteArray(byteArray.size)
            for ((index, byte) in byteArray.withIndex()) {
                val shiftedByte = shiftBitsLeft(byte, places)
                shiftedArray[index] = shiftedByte
                logger.trace("byte: ${StringUtils.byteToHex(byte)} shiftedByte: ${StringUtils.byteToHex(shiftedByte)}")
            }
            return shiftedArray
        }

        fun shiftBitsRight(byteArray: ByteArray, places: Int): ByteArray {
            var shiftedArray = ByteArray(byteArray.size)
            for ((index, byte) in byteArray.withIndex()) {
                val shiftedByte = shiftBitsRight(byte, places)
                shiftedArray[index] = shiftedByte
                logger.trace("byte: ${StringUtils.byteToHex(byte)} shiftedByte: ${StringUtils.byteToHex(shiftedByte)}")
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
            logger.trace("byte: ${StringUtils.byteToHex(byte)} intVal: ${StringUtils.intToHex(intVal)} maskedInt: ${StringUtils.intToHex(maskedInt)} shiftedInt: ${StringUtils.intToHex(shiftedInt)}")
            return shiftedInt.toByte()
        }

        fun shiftBitsRight(byte: Byte, places: Int): Byte {
            val intVal = byte.toInt()
            val maskedInt = maskInt(intVal, 0x000000FF)
            val shiftedInt = maskedInt shr places
            logger.trace("byte: ${StringUtils.byteToHex(byte)} shiftedByte: intVal: ${StringUtils.intToHex(intVal)} maskedInt: ${StringUtils.intToHex(maskedInt)} shiftedInt: ${StringUtils.intToHex(shiftedInt)}")
            return shiftedInt.toByte()
        }

        fun maskByte(byte: Byte, mask: Byte): Byte {
            return byte.and(mask)
        }

        fun maskInt(int: Int, mask: Int):Int {
            return int.and(mask)
        }

    }

}