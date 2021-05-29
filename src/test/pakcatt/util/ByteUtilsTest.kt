package pakcatt.util

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.experimental.and

class ByteUtilsTest {

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
    fun testMaskByte() {
        val originByte = subject.intToByte(0xFE)
        val mask = 0x81
        val expected = subject.intToByte(0x80)
        val result = subject.maskByte(originByte, mask)
        assertEquals(expected, result)
    }

    @Test
    fun testCompareMaskedByte() {
        val originByte = subject.intToByte(0xFE)
        val mask = 0xAA
        val comparison = 0xAA
        val result = subject.compareMaskedByte(originByte, mask, comparison)
        assertEquals(true, result)
    }

}