package pakcatt.network.radio.protocol.aprs.model

import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class APRSStatusFrame: APRSFrame() {

    /**
     * Parses the information field and returns any status text,
     * without other parameters such as date, beam heading and maidenhead locator.
     */
    fun statusText(): String {
        var payloadString = stringUtils.removeEOLChars(payloadDataString())
        return if (payloadString.length >= 2 && payloadString[0] == '>') {
            // We might need to remove other parameters from the status text
            val dateTimeStamp = dateTimeStamp()
            val beamHeadingAndERP = beamHeadingAndERP()
            val maidenhead = maidenheadGridLocator()

            // Start by stripping off the '>' character
            payloadString = payloadString.substring(1, payloadString.length)

            // Striping out any date or maidenhead fields
            if (null != dateTimeStamp) { // cut out the date field
                payloadString = payloadString.substring(7, payloadString.length)
            } else if (null != maidenhead && payloadString.length > maidenhead.length + 2) { // cut out the maidenhead field and following space
                payloadString = payloadString.substring(maidenhead.length + 3, payloadString.length)
            } else if (null != maidenhead) { // cut out just the maidenhead field
                payloadString = payloadString.substring(maidenhead.length + 2, payloadString.length)
            }

            // Lastly, remove any heading and EPR fields
            if (null != beamHeadingAndERP) { // cut out the beam heading and ERP field
                payloadString = payloadString.substring(0, payloadString.length - 3)
            }

            payloadString
        } else {
         ""
        }
    }

    /**
     * The datetime format in a status frame is >ddHHMMz where
     * > = start character in the information field
     * dd = day of month
     * HH = hour in 24 hours
     * MM = minute
     * We convert this format into a full datetimestamp so that
     * it's consistent and easily usable by higher layers of the application.
     *
     * Returns: an instance of LocalDateTime
     */
    fun dateTimeStamp(): LocalDateTime? {
        var payloadString = stringUtils.removeEOLChars(payloadDataString())
        return if (payloadString.length >= 8 && payloadString[0] == '>' && payloadString[7] == 'z') {
            // The year and month is missing, so we have to assume it's the current year and month
            val dateTimeNow = LocalDateTime.now()
            val aprsDateTimeString = payloadString.substring(1, 7)
            val completedDateTimeString = "${dateTimeNow.year}-${dateTimeNow.monthValue}-$aprsDateTimeString"
            var dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-M-ddHmm")
            LocalDateTime.parse(completedDateTimeString, dateTimeFormatter)
        } else {
            null
        }
    }

    /**
     * The location specified as a maidenhead grid locator code
     */
    fun maidenheadGridLocator(): String? {
        var payloadString = stringUtils.removeEOLChars(payloadDataString())
        return if (payloadString.length == 7 && payloadString[0] == '>') { // '>' plus 4 char maidenhead and 2 char symbol
            payloadString.substring(1, 5)
        } else if (payloadString.length == 9 && payloadString[0] == '>') { // '>' plus 6 char maidenhead and 2 char symbol
            payloadString.substring(1, 7)
        } else if (payloadString.length >= 8 && payloadString[0] == '>' && payloadString[7] == ' ') { // '>' plus 4 char maidenhead, 2 char symbol and text
            payloadString.substring(1, 5)
        } else if (payloadString.length >= 10 && payloadString[0] == '>' && payloadString[9] == ' ') { // '>' plus 6 char maidenhead, 2 char symbol and text
            payloadString.substring(1, 7)
        } else {
            null
        }
    }

    /**
     * The heading in degrees, and Effective Radiating Power (ERP)
     */
    fun beamHeadingAndERP(): HeadingAndERP? {
        var payloadString = stringUtils.removeEOLChars(payloadDataString())
        return if (payloadString.length >= 3 && payloadString[payloadString.length - 3] == '^') {
            val headingChar = payloadString[payloadString.length - 2]
            val erpChar = payloadString[payloadString.length - 1]
            val heading = resolveHeadingChar(headingChar)
            val erp = resolveERPChar(erpChar)
            HeadingAndERP(heading, erp)
        } else {
            null
        }
    }

    /**
     * The symbol code that, in part, represents the selected icon
     */
    fun symbolCode(): String? {
        val maidenheadGridLocator = maidenheadGridLocator()
        return if (null != maidenheadGridLocator) {
            stringUtils.convertByteToString(payloadData[maidenheadGridLocator.length + 1])
        } else {
            null
        }
    }

    /**
     * The table id that, in part, represents the selected icon
     */
    fun symbolTableId(): String? {
        val maidenheadGridLocator = maidenheadGridLocator()
        return if (null != maidenheadGridLocator) {
            stringUtils.convertByteToString(payloadData[maidenheadGridLocator.length + 2])
        } else {
            null
        }
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        val headingAndERP = beamHeadingAndERP()
        val location = maidenheadGridLocator()
        stringBuilder.append("Data Type: $aprsDataType ")
        stringBuilder.append("From: ${sourceCallsign()} ")
        if (repeaterCallsignOne.isNotEmpty()) {
            stringBuilder.append("Via1: ${repeaterCallsignOne()} ")
        }

        if (repeaterCallsignTwo.isNotEmpty()) {
            stringBuilder.append("Via2: ${repeaterCallsignTwo()} ")
        }
        stringBuilder.append("Timestamp: ${dateTimeStamp()} ")
        if (null != location) {
            stringBuilder.append("Grid: ${maidenheadGridLocator()} ")
            stringBuilder.append("Symbol: ${symbolCode()}${symbolTableId()} ")
        }
        if (null != headingAndERP) {
            stringBuilder.append("Heading: ${headingAndERP.heading} ")
            stringBuilder.append("ERP: ${headingAndERP.erp} ")
        }
        stringBuilder.append("Status: ${statusText()}")
        if (payloadData.isNotEmpty()) {
            stringBuilder.append(" Payload: ${payloadDataString()}")
        }
        return stringBuilder.toString()
    }

    private fun resolveHeadingChar(headingChar: Char): Int {
        return when (headingChar.toUpperCase()) {
            '0' -> 0 '1' -> 10 '2' -> 20 '3' -> 30
            '4' -> 40 '5' -> 50 '6' -> 60 '7' -> 70
            '8' -> 80 '9' -> 90 'A' -> 100 'B' -> 110
            'C' -> 120 'D' -> 130 'E' -> 140 'F' -> 150
            'G' -> 160 'H' -> 170 'I' -> 180 'J' -> 190
            'K' -> 200 'L' -> 210 'M' -> 220 'N' -> 230
            'O' -> 240 'P' -> 250 'Q' -> 260 'R' -> 270
            'S' -> 280 'T' -> 290 'U' -> 300 'V' -> 310
            'W' -> 320 'X' -> 330 'Y' -> 440 'Z' -> 350
            else -> -1
        }
    }

    private fun resolveERPChar(erpChar: Char): Int {
        return when (erpChar.toUpperCase()) {
            '0' -> 0 '1' -> 10 '2' -> 40 '3' -> 90
            '4' -> 160 '5' -> 250 '6' -> 360 '7' -> 490
            '8' -> 640 '9' -> 810 ':' -> 1000 ';' -> 1210
            '<' -> 1440 '=' -> 1690 '?' -> 2250 '@' -> 2560
            'A' -> 2890 'B' -> 3240 'C' -> 3610 'D' -> 4000
            'E' -> 4410 'F' -> 4840 'G' -> 5290 'H' -> 5760
            'I' -> 6250 'J' -> 6760 'K' -> 7290
            else -> -1
        }
    }

}