package pakcatt.util

import junit.framework.TestCase
import org.junit.Test

class ColumnFormatterTest : TestCase() {

    private val subject = ColumnFormatter(2, 5, 6, 8, 10)

    @Test
    fun testFormatLineAsColumns() {
        assertEquals("* col1 col2  col3    col4\n", subject.formatLineAsColumns("*", "col1", "col2", "col3", "col4"))
        assertEquals("* col1toolongcol2  col3    col4\n", subject.formatLineAsColumns("*", "col1toolong", "col2", "col3", "col4"))
        assertEquals("* col1 col2  col3    col4      missingindex\n", subject.formatLineAsColumns("*", "col1", "col2", "col3", "col4", "missingindex"))
        assertEquals("* col1 col2  col3    col4\n", subject.formatLineAsColumns("*", "col1", "col2", "col3", "col4"))
        assertEquals("\u001B[1m* col1 col2  col3    col4\u001B[0m\n", subject.formatLineAsColumns("*", "col1", "col2", "col3", "col4", isHeading = true))
    }
}