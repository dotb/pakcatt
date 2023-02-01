package pakcatt.util

import pakcatt.application.shared.FORMAT
import pakcatt.application.shared.TextFormat

/**
 * Format lines of text in columns using spaces and not tabs.
 * Instantiate an instance with column lengths, and then pass it
 * columns to format as lines of text that line up when printed vertically.
 */
class ColumnFormatter(private vararg val columnLengths: Int) {

    private val textFormat = TextFormat()
    private val stringUtils = StringUtils()

    fun formatLineAsColumns(vararg columns: String, isBold: Boolean = false): String {
        val stringBuilder = StringBuilder()
        if (isBold) {
            stringBuilder.append(textFormat.format(FORMAT.BOLD))
        }
        for ((index, columnString) in columns.withIndex()) {
            val columnLength = if (index < columnLengths.size) columnLengths[index] else columnString.length
            val requiredSpaceLength = columnLength - columnString.length
            stringBuilder.append(columnString)

            // Pad out the remaining column with spaces if we're not on the last column
            if (index < columns.size - 1) {
                for (i in 1..requiredSpaceLength) {
                    stringBuilder.append(" ")
                }
            }
        }
        if (isBold) {
            stringBuilder.append(textFormat.format(FORMAT.RESET))
        }
        stringBuilder.append(stringUtils.EOL)
        return stringBuilder.toString()
    }

}