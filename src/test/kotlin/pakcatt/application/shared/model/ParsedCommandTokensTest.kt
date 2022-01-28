package pakcatt.application.shared.model

import junit.framework.TestCase
import org.junit.Test

class ParsedCommandTokensTest : TestCase() {

    @Test
    fun `test dotted command line with multiple agruments`() {
        val parsedCommandTokens = ParsedCommandTokens().parseCommandLine("one.2.three.4.five")
        assertEquals("one", parsedCommandTokens.command())
        assertEquals(2, parsedCommandTokens.argumentAtIndexAsInt(1))
        assertEquals("three", parsedCommandTokens.argumentAtIndexAsString(2))
        assertEquals("4.five", parsedCommandTokens.remainingCommandLine())
    }

    @Test
    fun `test dotted command line with two agruments`() {
        val parsedCommandTokens = ParsedCommandTokens().parseCommandLine("one.2.three.4.five")
        assertEquals("one", parsedCommandTokens.command())
        assertEquals(2, parsedCommandTokens.argumentAtIndexAsInt(1))
        assertEquals("three.4.five", parsedCommandTokens.remainingCommandLine())
    }

    @Test
    fun `test dotted command line with one agrument`() {
        val parsedCommandTokens = ParsedCommandTokens().parseCommandLine("one")
        assertEquals("one", parsedCommandTokens.command())
        assertEquals("", parsedCommandTokens.remainingCommandLine())
    }

    @Test
    fun `test dotted command line without any query`() {
        val parsedCommandTokens = ParsedCommandTokens().parseCommandLine("one.2.three.4.five")
        assertEquals("one.2.three.4.five", parsedCommandTokens.remainingCommandLine())
    }

    fun `test dotted command line with only command query`() {
        val parsedCommandTokens = ParsedCommandTokens().parseCommandLine("one.2.three.4.five")
        assertEquals("one", parsedCommandTokens.command())
        assertEquals("2.three.4.five", parsedCommandTokens.remainingCommandLine())
    }

    @Test
    fun `test dotted command line badly formatted integer`() {
        val parsedCommandTokens = ParsedCommandTokens().parseCommandLine("one.2.three.4.five")
        assertEquals("one", parsedCommandTokens.command())
        assertEquals(2, parsedCommandTokens.argumentAtIndexAsInt(1))
        assertEquals(null, parsedCommandTokens.argumentAtIndexAsInt(2))
        assertEquals("4.five", parsedCommandTokens.remainingCommandLine())
    }

    @Test
    fun `test spaced command line with multiple arguments`() {
        val parsedCommandTokens = ParsedCommandTokens().parseCommandLine("one 2 three 4 five")
        assertEquals("one", parsedCommandTokens.command())
        assertEquals(2, parsedCommandTokens.argumentAtIndexAsInt(1))
        assertEquals("three", parsedCommandTokens.argumentAtIndexAsString(2))
        assertEquals("4 five", parsedCommandTokens.remainingCommandLine())
    }

    @Test
    fun `test empty command line`() {
        val parsedCommandTokens = ParsedCommandTokens().parseCommandLine("")
        assertEquals("", parsedCommandTokens.command())
        assertEquals(null, parsedCommandTokens.argumentAtIndexAsInt(1))
        assertEquals("", parsedCommandTokens.argumentAtIndexAsString(2))
        assertEquals("", parsedCommandTokens.remainingCommandLine())
    }

}