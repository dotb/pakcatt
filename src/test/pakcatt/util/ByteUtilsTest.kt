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

    @Test
    fun testFixEndOfLineCharacters() {
        val stringUtils = StringUtils()
        val lineFeed: Byte = 12
        val carriageReturn: Byte = 13
        val a: Byte = 97
        val b: Byte = 98
        val c: Byte = 99
        val d: Byte = 100

        val arrayWithCarriageReturnThenLineFeed = byteArrayOf(carriageReturn, lineFeed, a, b, carriageReturn, lineFeed, c, d, carriageReturn, lineFeed, carriageReturn, lineFeed, a, b, carriageReturn, lineFeed)
        val arrayWithLineFeedThenCarriageReturn = byteArrayOf(lineFeed, carriageReturn, a, b, lineFeed, carriageReturn, c, d, lineFeed, carriageReturn, lineFeed, carriageReturn, a, b, lineFeed, carriageReturn)
        val arrayWithOnlyLineFeed = byteArrayOf(lineFeed, a, b, lineFeed, c, d, lineFeed, lineFeed, a, b, lineFeed)
        val arrayWithOnlyCarriageReturn = byteArrayOf(carriageReturn, a, b, carriageReturn, c, d, carriageReturn, carriageReturn, a, b, carriageReturn)
        val shortArrayWithCarriageReturn = byteArrayOf(a, carriageReturn)
        val shortArrayWithLineFeed = byteArrayOf(a, lineFeed)
        val shortArrayWithCarriageReturnThenLineFeed = byteArrayOf(a, carriageReturn, lineFeed)
        val realPacketTest = byteArrayOf(0x61.toByte(), 0x0d.toByte(), 0x62.toByte(), 0x0d.toByte(), 0x63.toByte(), 0x0d.toByte(), 0x64.toByte(), 0x0d.toByte())
        val realPacketResult = byteArrayOf(a, carriageReturn, lineFeed, b, carriageReturn, lineFeed, c, carriageReturn, lineFeed, d, carriageReturn, lineFeed)


        // An array that already has both EOL characters should not be updated. We're not fussy about the order of EOL chars.
        assertEquals(stringUtils.byteArrayToHex(arrayWithCarriageReturnThenLineFeed), stringUtils.byteArrayToHex(subject.fixEndOfLineCharacters(arrayWithCarriageReturnThenLineFeed)))
        assertEquals(stringUtils.byteArrayToHex(arrayWithLineFeedThenCarriageReturn), stringUtils.byteArrayToHex(subject.fixEndOfLineCharacters(arrayWithLineFeedThenCarriageReturn)))

        // Arrays with only one EOL character should be adjusted to have both <CR> and <LF>
        assertEquals(stringUtils.byteArrayToHex(arrayWithCarriageReturnThenLineFeed), stringUtils.byteArrayToHex(subject.fixEndOfLineCharacters(arrayWithOnlyLineFeed)))
        assertEquals(stringUtils.byteArrayToHex(arrayWithCarriageReturnThenLineFeed), stringUtils.byteArrayToHex(subject.fixEndOfLineCharacters(arrayWithOnlyCarriageReturn)))

        // Short arrays should be handled, too.
        assertEquals(stringUtils.byteArrayToHex(shortArrayWithCarriageReturnThenLineFeed), stringUtils.byteArrayToHex(subject.fixEndOfLineCharacters(shortArrayWithCarriageReturn)))
        assertEquals(stringUtils.byteArrayToHex(shortArrayWithCarriageReturnThenLineFeed), stringUtils.byteArrayToHex(subject.fixEndOfLineCharacters(shortArrayWithLineFeed)))

        // Test an example for a real packet
        assertEquals(stringUtils.byteArrayToHex(realPacketResult), stringUtils.byteArrayToHex(subject.fixEndOfLineCharacters(realPacketTest)))
    }

}