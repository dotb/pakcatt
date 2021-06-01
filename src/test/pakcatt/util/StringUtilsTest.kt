package pakcatt.util

import junit.framework.TestCase
import org.junit.Test

class StringUtilsTest : TestCase() {


    private val subject = StringUtils()


    @Test
    fun testChompString() {
        val testString = "\r\ntest\n\r"
        val result = subject.chompString(testString)
        assertEquals("test", result)
    }
}