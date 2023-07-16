package pakcatt.network.radio.kiss.debug

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.KissFrameStandard
import pakcatt.util.StringUtils
import java.io.File
import java.util.*

/**
 * This class logs conversations between Pakcatt and remote stations into separate files.
 * There are two file types created for each conversation:
 * 1) Text files that are easy to follow during debugging.
 * 2) Test files that can be used to quickly create automated tests.
 */
@Component
class ConversationLogger(val myCall: String,
                         val conversationLogEnabled: Boolean,
                         val conversationLogPath: String) {

    private val logger = LoggerFactory.getLogger(ConversationLogger::class.java)
    private val stringUtils = StringUtils()

    fun logFrame(byteFrame: ByteArray) {
        if (conversationLogEnabled) {
            val kissFrame = KissFrameStandard().populateFromFrameData(byteFrame)
            val stringFrame = stringUtils.byteArrayToHex(byteFrame)
            logger.trace("Logging frame to conversation log: {}", stringFrame)
            when (kissFrame.destCallsign()) {
                myCall -> logReceivedFrame(kissFrame, stringFrame)
                else -> logSentFrame(kissFrame, stringFrame)
            }
        } else {
            logger.trace("Conversation logging is disabled. Not logging frame to conversation log")
        }
    }

    private fun logReceivedFrame(kissFrame: KissFrame, stringFrame: String) {
        val filenameTXT = "${myCall}_${kissFrame.sourceCallsign()}.txt"
        val datetime = Date()
        logToFile(filenameTXT, "$datetime RECEIVED: $kissFrame\n")

        val filenameTest = "${myCall}_${kissFrame.sourceCallsign()}.test"
        logToFile(filenameTest, "// $datetime SEND: $kissFrame\n")
        logToFile(filenameTest, "sendFrameFromBytesAndWaitResponse(mockedTNC, byteUtils.byteArrayFromStringInts(\"$stringFrame\"))\n")
    }

    private fun logSentFrame(kissFrame: KissFrame, stringFrame: String) {
        val filenameTXT = "${myCall}_${kissFrame.destCallsign()}.txt"
        val datetime = Date()
        logToFile(filenameTXT, "$datetime SENT:     $kissFrame\n")

        val filenameTest = "${myCall}_${kissFrame.destCallsign()}.test"
        logToFile(filenameTest, "// $datetime RECEIVE: $kissFrame\n")
        logToFile(filenameTest, "response = parseFramesFromResponse(mockedTNC.sentDataBuffer())\n")
        logToFile(filenameTest, "assertEquals(ControlField.${kissFrame.controlField()}, response[0].controlField())\n")
    }

    private fun logToFile(filename: String, logLine: String) {
        val logFilePath = "${conversationLogPath}/pakcatt_${filename}"
        try {
            val file = File(logFilePath)
            file.appendText("$logLine\n")
        } catch (e: Exception) {
            logger.error("Could not write to conversation log. Please check that the path {} is writable. Exception: {}", logFilePath, e.message)
        }
    }

}