package pakcatt.application.shared.model

import pakcatt.util.StringUtils
import java.lang.Integer.max
import java.lang.NumberFormatException

/**
 * This class holds a parsed command line and keeps
 * track of the consumed arguments, allowing it to return
 * the remaining, not yet consumed, command line parameters
 * that can be used to iterate through multiple apps
 * to service a user request in one line.
 */
class ParsedCommandTokens {

    private val stringUtils = StringUtils()
    private var stringTokens:List<String> = listOf()
    private var isInDottedNotation = false
    private var consumedArgumentIndex = -1

    /**
     * Parse the first argument on this input line, assuming the
     * first word is the command. For example, list 2 or list.2
     * This function returns the result in an object that includes
     * the String and Int representations (if possible) of the parsed argument
     * as well as any remaining instructions not yet parsed.
     */
    fun parseCommandLine(inputLine: String): ParsedCommandTokens {
        // Remove end-of-line, and whitespace chars at each end of the string
        val chompedInputLine = stringUtils.removeEOLChars(inputLine)
            .replace("^[\\s]+".toRegex(),"")
            .replace("[\\s]+$".toRegex(),"")
        consumedArgumentIndex = -1
        if (stringUtils.stringIsInDottedNotation(chompedInputLine)) {
            isInDottedNotation = true
            stringTokens = chompedInputLine.split(".")
        } else {
            isInDottedNotation = false
            stringTokens = chompedInputLine.split(" ")
        }
        return this
    }

    fun command(): String {
        consumedArgumentIndex = max(consumedArgumentIndex, 0)
        return argumentAtIndexAsString(0)
    }

    fun argumentAtIndexAsInt(index: Int): Int? {
        val stringArgument = argumentAtIndexAsString(index)
        return try {
            stringArgument.toInt()
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun argumentAtIndexAsString(index: Int): String {
        return if (stringTokens.size > index) {
            consumedArgumentIndex = max(consumedArgumentIndex, index)
            stringTokens[index]
        } else {
            ""
        }
    }

    fun remainingCommandLine(): String {
        val stringBuilder = StringBuilder()
        for (i in consumedArgumentIndex + 1..stringTokens.size - 1) {
            if (i > consumedArgumentIndex + 1) {
                if (isInDottedNotation) stringBuilder.append(".") else stringBuilder.append(" ")
            }
            stringBuilder.append(stringTokens[i])
        }
        return stringBuilder.toString()
    }

}
