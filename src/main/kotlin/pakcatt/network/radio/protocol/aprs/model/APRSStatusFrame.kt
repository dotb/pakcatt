package pakcatt.network.radio.protocol.aprs.model

import java.lang.StringBuilder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class APRSStatusFrame: APRSFrame() {

    // This data structure maps APRS heading character to numeric heading values
    private val headingMap = mapOf(Pair('0', 0), Pair('1',10), Pair('2', 20), Pair('3', 30),
                                   Pair('4', 40), Pair('5',50), Pair('6', 60), Pair('7', 70),
                                   Pair('8', 80), Pair('9',90), Pair('A', 100), Pair('B', 110),
                                   Pair('C', 120), Pair('D',130), Pair('E', 140), Pair('F', 150),
                                   Pair('G', 160), Pair('H',170), Pair('I', 180), Pair('J', 190),
                                   Pair('K', 200), Pair('L',210), Pair('M', 220), Pair('N', 230),
                                   Pair('O', 240), Pair('P',250), Pair('Q', 260), Pair('R', 270),
                                   Pair('S', 280), Pair('T',290), Pair('U', 300), Pair('V', 310),
                                   Pair('W', 320), Pair('X',330), Pair('Y', 440), Pair('Z', 350))

    // This data structure maps APRS ERP characters to numeric values in watts
    private val erpMap = mapOf(Pair('0', 0), Pair('1', 10), Pair('2', 40), Pair('3', 90),
                               Pair('4', 160), Pair('5', 250), Pair('6', 360), Pair('7', 490),
                               Pair('8', 640), Pair('9', 810), Pair(':', 1000), Pair(';', 1210),
                               Pair('<', 1440), Pair('=', 1690), Pair('?', 2250), Pair('@', 2560),
                               Pair('A', 2890), Pair('B', 2340), Pair('C', 3610), Pair('D', 4000),
                               Pair('E', 4410), Pair('F', 4840), Pair('G', 5290), Pair('H', 5760),
                               Pair('I', 6250), Pair('J', 6760), Pair('K', 7290))

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
        return headingMap[headingChar]?: -1
    }

    private fun resolveERPChar(erpChar: Char): Int {
        return erpMap[erpChar]?: -1
    }

}