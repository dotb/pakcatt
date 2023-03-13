package pakcatt.network.radio.protocol.aprs.model

import java.lang.StringBuilder


enum class MIC_E {
    OFF_DUTY, EN_ROUTE, IN_SERVICE, RETURNING, COMMITTED, SPECIAL, PRIORITY, CUSTOM_0,
    CUSTOM_1, CUSTOM_2, CUSTOM_3, CUSTOM_4, CUSTOM_5, CUSTOM_6, EMERGENCY, UNKNOWN
}

/* NOTE - the 101 APRS spec PDF is out of date.
 * The telemetry was abandonded and instead
 * the field is used for compabitility
 * http://www.aprs.org/aprs12/mic-e-types.txt */
enum class RadioCompatibility {
    BEACON,
    MESSAGE,
    UNKNOWN
}

class APRSMicEDataFrame: APRSFrame() {

    // These are manufacture / radio model codes found on the end of a status strings
    private val radioModelCodes = listOf<String>("v", "=", "v", "=", "^", "Mv", "Mv", "_b", "_\"", "_#", "_$",
        "_%", "_\\)", "_\\(", "_0", "_1", " X", "\\(5", "\\(8", "|3", "|4", "\\v", "/v", "^v", "\\*v", ":4", ":8", "~v",
        "`v", "'v", "/v", "-v", ":v", ";v", "v")

    init {
        aprsDataType = APRSDataType.MIC_E_DATA
    }

    fun latitudeDecimalDegreesNorth(): Double {
        val degrees = latitudeDegrees().replace("?", "0").toDouble()
        val minutes = latitudeMinutes().replace("?", "0").toDouble()
        val hundredthsOfAMinute = latitudeHundredths().replace("?", "0").toDouble()
        val latDegrees = degrees + (minutes/60) + (hundredthsOfAMinute/100/60)
        return if (latitudeNorthSouth() == "S") {
            0 - latDegrees
        } else {
            latDegrees
        }
    }

    fun longitudeDecimalDegreesEast(): Double {
        val degrees = longitudeDegrees().replace("?", "0").toDouble()
        val minutes = longitudeMinutes().replace("?", "0").toDouble()
        val hundredthsOfAMinute = longitudeHundredths().replace("?", "0").toDouble()
        val lonDegrees = degrees + (minutes/60) + (hundredthsOfAMinute/100/60)
        return if (longitudeWestEast() == "W") {
            0 - lonDegrees
        } else {
            lonDegrees
        }
    }

    fun latitudeDegreesMinutesHundredths(): String {
        return latitudeDegreesMinutesHundredthsWithAmbiguity().replace("?", "0")
    }

    fun latitudeDegreesMinutesHundredthsWithAmbiguity(): String {
        return "${latitudeDegrees()}.${latitudeMinutes()}.${latitudeHundredths()}${latitudeNorthSouth()}"
    }

    fun latitudeDegrees(): String {
        val firstDigit = decodeDigitForByte(destCallsign[0])
        val secondDigit = decodeDigitForByte(destCallsign[1])
        return "$firstDigit$secondDigit"
    }

    fun latitudeMinutes(): String {
        val firstDigit = decodeDigitForByte(destCallsign[2])
        val secondDigit = decodeDigitForByte(destCallsign[3])
        return "$firstDigit$secondDigit"
    }

    fun latitudeHundredths(): String {
        val firstDigit = decodeDigitForByte(destCallsign[4])
        val secondDigit = decodeDigitForByte(destCallsign[5])
        return "$firstDigit$secondDigit"
    }

    fun latitudeNorthSouth(): String {
        return decodeNorthSouthForByte(destCallsign[3])
    }

    fun longitudeDegreesMinutesHundredths(): String {
        return longitudeDegreesMinutesHundredthsWithAmbiguity().replace("?", "0")
    }

    fun longitudeDegreesMinutesHundredthsWithAmbiguity(): String {
        // Both longitude and latitude values share the same level of ambiguity
        var longitudeString = "${longitudeDegrees()}.${longitudeMinutes()}.${longitudeHundredths()}${longitudeWestEast()}"
        val latitudeString = latitudeDegreesMinutesHundredthsWithAmbiguity()
        for ((index, char) in latitudeString.withIndex()) {
            if (char == '?') {
                longitudeString = longitudeString.replaceRange(index + 1, index + 2, "?")
            }
        }
        return longitudeString
    }

    fun longitudeDegrees(): String {
        val degreesPlusTwentyEight = byteUtils.byteToInt(payloadData[1])
        return decodeLongitudeDegrees(degreesPlusTwentyEight)
    }

    fun longitudeMinutes(): String {
        val minutesPlusTwentyEight = byteUtils.byteToInt(payloadData[2])
        return decodeLongitudeMinutes(minutesPlusTwentyEight)
    }

    fun longitudeHundredths(): String {
        val hundredthsPlusTwentyEight = byteUtils.byteToInt(payloadData[3])
        return decodeLongitudeHundredths(hundredthsPlusTwentyEight)
    }

    fun longitudeWestEast(): String {
        return decodeWestEastForByte(destCallsign[5])
    }

    fun micEType(): MIC_E {
        return decodeMicEForBytes(destCallsign[0], destCallsign[1], destCallsign[2])
    }

    fun longitudeOffset(): Int {
        return decodeLongitudeOffset(destCallsign[4])
    }

    fun speedKnots(): Double {
        return decodeSpeedKnots(byteUtils.byteToInt(payloadData[4]), byteUtils.byteToInt(payloadData[5]))
    }

    fun speedKmh(): Double {
        return speedKnots() * 1.852001
    }

    fun courseDegrees(): Int {
        return decodeDirectionDegrees(byteUtils.byteToInt(payloadData[5]), byteUtils.byteToInt(payloadData[6]))
    }

    fun symbolCode(): String {
        return stringUtils.convertByteToString(payloadData[7])
    }

    fun symbolTableId(): String {
        return stringUtils.convertByteToString(payloadData[8])
    }

    /**
     *  See page 54 of the APRS Spec on telemetry, but note it was abandoned
     *  in favor of defining radio compatibility described here:
     *  http://www.aprs.org/aprs12/mic-e-types.txt
     *  If the 9th byte is:
     *  ‘ = 0x60 = message compatible device
     *  ' = 0x27 = beacon / location only compatible device
     */
    fun radioCompatibility(): RadioCompatibility {
        return if (payloadData.size >= 9) {
            when (payloadData[8].toChar()) {
                ' ' -> RadioCompatibility.BEACON
                '\'' -> RadioCompatibility.BEACON
                'T' -> RadioCompatibility.BEACON
                '>' -> RadioCompatibility.MESSAGE
                ']' -> RadioCompatibility.MESSAGE
                '`' -> RadioCompatibility.MESSAGE
                else -> RadioCompatibility.UNKNOWN
            }
        } else {
            RadioCompatibility.UNKNOWN
        }
    }

    fun hasAltitude(): Boolean {
        return payloadData.size >= 14 && payloadData[13].toChar() == '}'
    }

    fun statusText(): String {
        val payloadString = payloadDataString()
        // The status text starts after the symbol bytes, if there isn't telemetry data then it starts after the telemetry data
        var startIndex = when (hasAltitude()) {
            true -> 14
            else -> 10
        }
        val endIndex = payloadString.length - 1

        var statusString = if (startIndex < endIndex) {
            payloadString.substring(startIndex, endIndex)
        } else {
            ""
        }

        // Clean up any manufacturer / radio model characters from the end of the string
        for (modelStr in radioModelCodes) {
            val regex = "${modelStr}\$".toRegex()
            statusString = statusString.replace(regex, "")
        }
        return statusString
    }

    /**
     * Return the ambiguity of the location
     * 0 - no ambiguity
     * 1 or more - the number of digits of ambiguity missing
     * in the lat and lon values
     */
    fun ambiguity(): Int {
        val latitude = latitudeDegreesMinutesHundredthsWithAmbiguity()
        return latitude.count { "?".contains(it) }
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Data Type: $aprsDataType ")
        stringBuilder.append("From: ${sourceCallsign()} ")
        if (repeaterCallsignOne.isNotEmpty()) {
            stringBuilder.append("Via1: ${repeaterCallsignOne()} ")
        }

        if (repeaterCallsignTwo.isNotEmpty()) {
            stringBuilder.append("Via2: ${repeaterCallsignTwo()} ")
        }
        stringBuilder.append("Lat: ${latitudeDecimalDegreesNorth()} ")
        stringBuilder.append("Lon: ${longitudeDecimalDegreesEast()} ")
        stringBuilder.append("Ambiguity: ${ambiguity()} ")
        stringBuilder.append("Speed: ${speedKmh()}km/h ")
        stringBuilder.append("Knots: ${speedKnots()} ")
        stringBuilder.append("Course: ${courseDegrees()} ")
        stringBuilder.append("Symbol: ${symbolCode()}${symbolTableId()} ")
        stringBuilder.append("Compat: ${radioCompatibility()} ")
        stringBuilder.append("Status: ${statusText()}")
        if (payloadData.isNotEmpty()) {
            stringBuilder.append(" Payload: ${payloadDataString()}")
        }
        return stringBuilder.toString()
    }

    private fun decodeDigitForByte(rawByte: Byte): String {
        val keyChar = getKeyChar(rawByte)
        return when {
            listOf("0", "A", "P").contains(keyChar) -> "0"
            listOf("1", "B", "Q").contains(keyChar) -> "1"
            listOf("2", "C", "R").contains(keyChar) -> "2"
            listOf("3", "D", "S").contains(keyChar) -> "3"
            listOf("4", "E", "T").contains(keyChar) -> "4"
            listOf("5", "F", "U").contains(keyChar) -> "5"
            listOf("6", "G", "V").contains(keyChar) -> "6"
            listOf("7", "H", "W").contains(keyChar) -> "7"
            listOf("8", "I", "X").contains(keyChar) -> "8"
            listOf("9", "J", "Y").contains(keyChar) -> "9"
            listOf("K", "L", "Z").contains(keyChar) -> "?" // Space or ambiguous digit
            else -> "?" // Unknown
        }
    }

    private fun decodeNorthSouthForByte(rawByte: Byte): String {
        val keyChar = getKeyChar(rawByte)
        return when {
            listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "L").contains(keyChar) -> "S"
            listOf("P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z").contains(keyChar) -> "N"
            else -> "?"
        }
    }

    private fun decodeWestEastForByte(rawByte: Byte): String {
        val keyChar = getKeyChar(rawByte)
        return when {
            listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "L").contains(keyChar) -> "E"
            listOf("P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z").contains(keyChar) -> "W"
            else -> "?"
        }
    }

    /**
     * Decode the MIC-E type. The S and C characters mark 1 bits as standard or custom bits.
     * 1S is a standard 1 bit while 1C is a custom 1 bit. 0 is a 0 bit for both custom and standard
     * MIC-E types.
     */
    private fun decodeMicEForBytes(byteA: Byte, byteB: Byte, byteC: Byte): MIC_E {
        val micBitA = getMicEBitForByte(byteA)
        val micBitB = getMicEBitForByte(byteB)
        val micBitC = getMicEBitForByte(byteC)
        return when ("$micBitA$micBitB$micBitC") {
            "000" -> MIC_E.EMERGENCY
            "001S" -> MIC_E.PRIORITY
            "001C" -> MIC_E.CUSTOM_6
            "01S0" -> MIC_E.SPECIAL
            "01C0" -> MIC_E.CUSTOM_5
            "01S1S" -> MIC_E.COMMITTED
            "01C1C" -> MIC_E.CUSTOM_4
            "1S00" -> MIC_E.RETURNING
            "1C00" -> MIC_E.CUSTOM_3
            "1S01S" -> MIC_E.IN_SERVICE
            "1C01C" -> MIC_E.CUSTOM_2
            "1S1S0" -> MIC_E.EN_ROUTE
            "1C1C0" -> MIC_E.CUSTOM_1
            "1S1S1S" -> MIC_E.OFF_DUTY
            "1C1C1C" -> MIC_E.CUSTOM_0
            else -> MIC_E.UNKNOWN
        }
    }

    /**
     * 0 means 0
     * 1C means the bit is set to 1 for a custom MIC-E
     * 1S means the bit is set to 1 for a standard MIC-E
     */
    private fun getMicEBitForByte(byte: Byte): String {
        val keyChar = getKeyChar(byte)
        return when {
            listOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "L").contains(keyChar) -> "0"
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K").contains(keyChar) -> "1C"
            listOf("P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z").contains(keyChar) -> "1S"
            else -> "0"
        }
    }

    private fun decodeLongitudeOffset(byte: Byte): Int {
        val keyChar = getKeyChar(byte)
        return when {
            listOf("P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z").contains(keyChar) -> 100
            else -> 0
        }
    }

    private fun getKeyChar(byte: Byte): String {
        val shiftedByte = byteUtils.shiftBitsRight(byte, 1)
        return stringUtils.convertByteToString(shiftedByte)
    }

    /**
     * From APRS Spec page 48:
     * To decode the longitude degrees value:
        1. subtract 28 from the d+28 value to obtain d.
        2. if the longitude offset is +100 degrees, add 100 to d.
        3. subtract 80 if 180 <= d <= 189
        (i.e. the longitude is in the range 100–109 degrees).
        4. or, subtract 190 if 190 <= d <= 199.
        (i.e. the longitude is in the range 0–9 degrees).
     */
    private fun decodeLongitudeDegrees(degreesPlusTwentyEight: Int): String {
        // 1. subtract 28 from the d+28 value to obtain d.
        var degrees = degreesPlusTwentyEight - 28

        // 2. if the longitude offset is +100 degrees, add 100 to d.
        degrees += longitudeOffset()

        // 3. subtract 80 if 180 <= d <= 189 (i.e. the longitude is in the range 100–109 degrees).
        if (degrees in 180..189) {
            degrees -= 80
        } else if (degrees in 190..199) {
            // 4. or, subtract 190 if 190 <= d <= 199. (i.e. the longitude is in the range 0–9 degrees).
            degrees -= 190
        }
        return if (degrees <= 9) {
            "0$degrees"
        } else {
            degrees.toString()
        }
    }

    /**
     * * From APRS Spec page 49:
     * To decode the longitude minutes value:
        1. subtract 28 from the m+28 value to obtain m.
        2. subtract 60 if m >= 60.
        (i.e. the longitude minutes is in the range 0–9).
     */
    private fun decodeLongitudeMinutes(minutesPlusTwentyEight: Int): String {
        // 1. subtract 28 from the m+28 value to obtain m.
        var minutes = minutesPlusTwentyEight - 28
        // 2. subtract 60 if m >= 60. (i.e. the longitude minutes is in the range 0–9).
        if (minutes >= 60) {
            minutes -= 60
        }
        return if (minutes <= 9) {
            "0$minutes"
        } else {
            minutes.toString()
        }
    }

    /**
     * From APRS Spec page 49
     * To decode the longitude hundredths of minutes value, subtract 28 from the h+28 value.
     */
    private fun decodeLongitudeHundredths(hundredthsPlusTwentyEight: Int): String {
        val hundredths = hundredthsPlusTwentyEight - 28
        return if (hundredths <= 9) {
            "0$hundredths"
        } else {
            hundredths.toString()
        }
    }

    /**
     * From APRS Spec page 52
     * To decode the speed and course:
        SP+28: To obtain the speed in tens of knots, subtract 28 from the SP+28 value and multiply by 10.
        DC+28: Subtract 28 from the DC+28 value and divide the result by 10. The quotient is the units of speed. The remainder is the course in hundreds of degrees.
        SE+28: To obtain the tens and units of degrees, subtract 28 from the SE+28 value.
        If the computed speed is >= 800 knots, subtract 800.
     */
    private fun decodeSpeedKnots(speedPlusTwentyEight: Int, directionPlusTwentyEight: Int): Double {
        // SP+28: To obtain the speed in tens of knots, subtract 28 from the SP+28 value and multiply by 10.
        var speed = (speedPlusTwentyEight - 28) * 10
        // DC+28: Subtract 28 from the DC+28 value and divide the result by 10. The quotient is the units of speed.
        val speedUnits = (directionPlusTwentyEight - 28) / 10
        speed += speedUnits
        // If the computed speed is >= 800 knots, subtract 800.
        return if (speed >= 800) {
            speed.toDouble() - 800
        } else {
            speed.toDouble()
        }
    }

    /**
     * From APRS Spec page 52
     * To decode the speed and course:
        DC+28: Subtract 28 from the DC+28 value and divide the result by 10. The quotient is the units of speed. The remainder is the course in hundreds of degrees.
        SE+28: To obtain the tens and units of degrees, subtract 28 from the SE+28 value.
        If the computed course is >= 400 degrees, subtract 400.
     */
    private fun decodeDirectionDegrees(directionPlusTwentyEight: Int, directionUnitsPlusTwentyEight: Int): Int {
        // DC+28: Subtract 28 from the DC+28 value and divide the result by 10. The remainder is the course in hundreds of degrees.
        var direction = (directionPlusTwentyEight - 28) % 10 * 100

        // SE+28: To obtain the tens and units of degrees, subtract 28 from the SE+28 value.
        val directionTensUnits = directionUnitsPlusTwentyEight - 28
        direction += directionTensUnits
        // If the computed course is >= 400 degrees, subtract 400.
        return if (direction >= 400) {
            direction - 400
        } else {
            direction
        }
    }

}