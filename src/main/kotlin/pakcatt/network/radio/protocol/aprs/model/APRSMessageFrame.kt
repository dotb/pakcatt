package pakcatt.network.radio.protocol.aprs.model

import java.lang.NumberFormatException
import java.lang.StringBuilder

class APRSMessageFrame: APRSFrame() {
    /*
    Example payload for message from VK3LIT-7 to VK2VRO-7
    :  V  K  2  V  R  O  -  7  SP :  H  e  l  l  o  {  4  7  END
    3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d
     */

    companion object {
        const val MAX_CONTENT_LENGTH = 67
        const val MAX_MESSAGE_NUMBER_LENGTH = 5
        const val MAX_MESSAGE_NUMBER = 99999
        const val MAX_DEST_CALLSIGN_LENGTH = 9
    }

    private var messageDestinationCallsign = ""
    private var message = ""
    private var messageNumber = -1

    init {
        aprsDataType = APRSDataType.MESSAGE
    }

    fun setMessageSourceCallsign(sourceCallsign: String) {
        setSourceCallsign(sourceCallsign)
    }

    fun setMessageDestinationCallsign(destinationCallsign: String) {
        this.messageDestinationCallsign = stringUtils.formatCallsignEnsureSSID(destinationCallsign)
        updateFrameFieldsUsingAPRSMessageParameters()
    }

    fun setMessage(message: String) {
        this.message = message
        updateFrameFieldsUsingAPRSMessageParameters()
    }

    fun setMessageNumber(messageNumber: Int) {
        this.messageNumber = messageNumber
        updateFrameFieldsUsingAPRSMessageParameters()
    }

    fun messageSourceCallsign(): String {
        return sourceCallsign()
    }

    fun messageDestinationCallsign(): String {
        return messageDestinationCallsign
    }

    fun message(): String {
        return message
    }

    fun messageNumber(): Int {
        return messageNumber
    }

    override fun populateFromFrameData(frameByteData: ByteArray): APRSMessageFrame {
        super.populateFromFrameData(frameByteData)
        // Update our local message variables
        val payloadString = payloadDataString()
        setMessageDestinationCallsignFromPayload(payloadString)
        setMessageNumberFromPayload(payloadString)
        setMessageFromPayload(payloadString)
        return this
    }

    override fun packetData(): ByteArray {
        updateFrameFieldsUsingAPRSMessageParameters()
        return super.packetData()
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Data Type: $aprsDataType ")
        stringBuilder.append("From: ${sourceCallsign()} ")
        stringBuilder.append("To: ${messageDestinationCallsign()} ")
        if (repeaterCallsignOne.isNotEmpty()) {
            stringBuilder.append("Via1: ${repeaterCallsignOne()} ")
        }

        if (repeaterCallsignTwo.isNotEmpty()) {
            stringBuilder.append("Via2: ${repeaterCallsignTwo()} ")
        }
        stringBuilder.append("No: ${messageNumber()} ")
        stringBuilder.append("Message: ${message()} ")
        if (payloadData.isNotEmpty()) {
            stringBuilder.append("Payload: ${payloadDataString()}")
        }
        return stringBuilder.toString()
    }

    private fun setMessageDestinationCallsignFromPayload(payloadDataString: String) {
        var callsign = payloadDataString.split(":")[1]
        callsign = stringUtils.removeWhitespace(callsign)
        this.messageDestinationCallsign = callsign
    }

    private fun setMessageFromPayload(payloadDataString: String) {
        var parsedMessage = stringUtils.removeEOLChars(payloadDataString)
        // Find the second instance of ':' That's where the message starts
        val startIndex = parsedMessage.indexOf(":", 1) + 1
        parsedMessage = parsedMessage.substring(startIndex, parsedMessage.length)
        // Remove the message ID, if there is one.
        if (messageNumber > -1 && parsedMessage.contains("{")) {
            val indexOfMessageId = parsedMessage.lastIndexOf("{")
            parsedMessage = parsedMessage.substring(0, indexOfMessageId)
        }
        this.message = parsedMessage
    }

    private fun setMessageNumberFromPayload(payloadDataString: String) {
        try {
            var cleanedPayloadString = stringUtils.removeEOLChars(payloadDataString)
            if (cleanedPayloadString.contains("{")) {
                val indexOfMessageId = cleanedPayloadString.lastIndexOf("{") + 1
                var messageNumberString = payloadDataString().substring(indexOfMessageId, cleanedPayloadString.length)
                this.messageNumber = messageNumberString.toInt()
            } else {
                this.messageNumber = -1
            }
        } catch (e: NumberFormatException) {
            this.messageNumber = -1
        }
    }

    private fun updateFrameFieldsUsingAPRSMessageParameters() {
        // The destination callsign must be 9 characters in length and message numbers 5
        this.message = stringUtils.trimmedString(message, MAX_CONTENT_LENGTH)
        this.messageNumber = stringUtils.trimmedString(messageNumber.toString(), MAX_MESSAGE_NUMBER_LENGTH).toInt()
        this.messageDestinationCallsign = stringUtils.trimmedString(messageDestinationCallsign, MAX_DEST_CALLSIGN_LENGTH)
        val fixedLengthDestination = stringUtils.fixedSizeString(messageDestinationCallsign, MAX_DEST_CALLSIGN_LENGTH)
        // Construct the payload using our local variables
        val stringPayloadBuilder = StringBuilder()
        stringPayloadBuilder.append(":")
        stringPayloadBuilder.append(fixedLengthDestination)
        stringPayloadBuilder.append(":")
        stringPayloadBuilder.append(message)
        if (messageNumber in 0..MAX_MESSAGE_NUMBER) {
            stringPayloadBuilder.append("{")
            stringPayloadBuilder.append(messageNumber)
        }
        this.payloadData = stringUtils.convertStringToBytes(stringPayloadBuilder.toString())
    }

}