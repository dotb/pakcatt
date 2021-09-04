package pakcatt.util

import junit.framework.TestCase
import org.junit.Test

class StringUtilsTest : TestCase() {


    private val subject = StringUtils()


    @Test
    fun testChompString() {
        val testString = "\r\ntest\n\r"
        val result = subject.removeEOLChars(testString)
        assertEquals("test", result)
    }

    @Test
    fun testFixEndOfLineCharacters() {
        val lineFeed: Byte = 10
        val carriageReturn: Byte = 13
        val a: Byte = 97
        val b: Byte = 98
        val c: Byte = 99
        val d: Byte = 100

        val targetEOLSequence = String(byteArrayOf(carriageReturn, lineFeed))
        val arrayWithCarriageReturnThenLineFeed = String(byteArrayOf(carriageReturn, lineFeed, a, b, carriageReturn, lineFeed, c, d, carriageReturn, lineFeed, carriageReturn, lineFeed, a, b, carriageReturn, lineFeed))
        val arrayWithLineFeedThenCarriageReturn = String(byteArrayOf(lineFeed, carriageReturn, a, b, lineFeed, carriageReturn, c, d, lineFeed, carriageReturn, lineFeed, carriageReturn, a, b, lineFeed, carriageReturn))
        val arrayWithOnlyLineFeed = String(byteArrayOf(lineFeed, a, b, lineFeed, c, d, lineFeed, lineFeed, a, b, lineFeed))
        val arrayWithOnlyCarriageReturn = String(byteArrayOf(carriageReturn, a, b, carriageReturn, c, d, carriageReturn, carriageReturn, a, b, carriageReturn))
        val shortArrayWithCarriageReturn = String(byteArrayOf(a, carriageReturn))
        val shortArrayWithLineFeed = String(byteArrayOf(a, lineFeed))
        val shortArrayWithCarriageReturnThenLineFeed = String(byteArrayOf(a, carriageReturn, lineFeed))
        val realPacketTest = String(byteArrayOf(0x61.toByte(), 0x0d.toByte(), 0x62.toByte(), 0x0d.toByte(), 0x63.toByte(), 0x0d.toByte(), 0x64.toByte(), 0x0d.toByte()))
        val realPacketResult = String(byteArrayOf(a, carriageReturn, lineFeed, b, carriageReturn, lineFeed, c, carriageReturn, lineFeed, d, carriageReturn, lineFeed))


        // An array that already has both EOL characters should not be updated. We're not fussy about the order of EOL chars.
        assertEquals(subject.stringToHex(arrayWithCarriageReturnThenLineFeed), subject.stringToHex(subject.fixEndOfLineCharacters(arrayWithCarriageReturnThenLineFeed, targetEOLSequence)))
        assertEquals(subject.stringToHex(arrayWithCarriageReturnThenLineFeed), subject.stringToHex(subject.fixEndOfLineCharacters(arrayWithLineFeedThenCarriageReturn, targetEOLSequence)))

        // Arrays with only one EOL character should be adjusted to have both <CR> and <LF>
        assertEquals(subject.stringToHex(arrayWithCarriageReturnThenLineFeed), subject.stringToHex(subject.fixEndOfLineCharacters(arrayWithOnlyLineFeed, targetEOLSequence)))
        assertEquals(subject.stringToHex(arrayWithCarriageReturnThenLineFeed), subject.stringToHex(subject.fixEndOfLineCharacters(arrayWithOnlyCarriageReturn, targetEOLSequence)))

        // Short arrays should be handled, too.
        assertEquals(subject.stringToHex(shortArrayWithCarriageReturnThenLineFeed), subject.stringToHex(subject.fixEndOfLineCharacters(shortArrayWithCarriageReturn, targetEOLSequence)))
        assertEquals(subject.stringToHex(shortArrayWithCarriageReturnThenLineFeed), subject.stringToHex(subject.fixEndOfLineCharacters(shortArrayWithLineFeed, targetEOLSequence)))

        // Test an example for a real packet
        assertEquals(realPacketResult,subject.fixEndOfLineCharacters(realPacketTest, targetEOLSequence))
    }


}