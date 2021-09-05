package pakcatt.application.shared

enum class FORMAT(val ansiCode: Int) {
    RESET(0),
    BOLD(1),
    DIM(2),
    STANDOUT(3),
    UNDERLINE(4),
    BLINK(5),
    INVERT(7),
    HIDDEN(8)
}

enum class COLOUR(val ansiCode: Int) {
    BLACK(0),
    RED(1),
    GREEN(2),
    YELLOW(3),
    BLUE(4),
    MAGENTA(5),
    CYAN(6),
    WHITE(7),
    DEFAULT(9)
}

open class TextFormat {

    val escapeChar = 27.toChar()

    fun format(formatCode: FORMAT): String {
        return "$escapeChar[${formatCode.ansiCode}m"
    }

    fun fgColour(colourCode: COLOUR): String {
        return "$escapeChar[3${colourCode.ansiCode}m"
    }

    fun bgColour(colourCode: COLOUR): String {
        return "$escapeChar[4${colourCode.ansiCode}m"
    }

}