package pakcatt.util

import junit.framework.TestCase
import org.junit.Test

class ByteUtilsTest: TestCase() {

    private val subject = ByteUtils()

    @Test
    fun testByteToInt() {
        var byte: Byte = 0xFF.toByte()
        var int = subject.byteToInt(byte)
        assertEquals(255, int)

        byte = 0x80.toByte()
        int = subject.byteToInt(byte)
        assertEquals(128, int)

        byte = -86 // 10101010
        int = subject.byteToInt(byte)
        assertEquals(0xAA, int)
    }

    @Test
    fun testIntToByte() {
        var int: Int = -1
        var byte = subject.intToByte(int)
        assertEquals(0xFF.toByte(), byte)

        int = -128
        byte = subject.intToByte(int)
        assertEquals(0x80.toByte(), byte)

        int = 0x100
        byte = subject.intToByte(int)
        assertEquals(0x00.toByte(), byte)
    }

    @Test
    fun testsSiftBitsRightInt() {
        var originalInt = 0x00FF00FF
        var result = subject.shiftBitsRight(originalInt, 4)
        assertEquals(0x0F, result)

        originalInt = 0xAA
        result = subject.shiftBitsRight(originalInt, 1)
        assertEquals(0x55, result)
    }


    @Test
    fun testMaskByte() {
        val originByte = subject.intToByte(0xFE)
        val mask = 0x81
        val expected = subject.intToByte(0x80)
        val result = subject.maskByte(originByte, mask)
        assertEquals(expected, result)
    }

    @Test
    fun testCompareMaskedByte() {
        var originByte = subject.intToByte(0xFE)
        var mask = 0xAA
        var comparison = 0xAA
        var result = subject.compareMaskedByte(originByte, mask, comparison)
        assertEquals(true, result)

        originByte = subject.intToByte(0x3F)
        mask = 0x10
        comparison = 0x00
        result = subject.compareMaskedByte(originByte, mask, comparison)
        assertEquals(false, result)

        originByte = subject.intToByte(0x3f)
        mask = 0x00
        comparison = 0x00
        result = subject.compareMaskedByte(originByte, mask, comparison)
        assertEquals(true, result)
    }

}