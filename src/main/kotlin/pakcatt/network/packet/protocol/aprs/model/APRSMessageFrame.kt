package pakcatt.network.packet.protocol.aprs.model

import java.lang.StringBuilder

class APRSMessageFrame: APRSFrame() {
    /*
    Example payload for message from VK3LIT-7 to VK2VRO-7
    :  V  K  2  V  R  O  -  7  SP :  H  e  l  l  o  {  4  7  END
    3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d
     */

    private var messageDestinationCallsign = ""
    private var message = ""
    private var messageNumber = -1

    init {
        aprsDataType = APRSDataType.MESSAGE
    }

    override fun populateFromFrameData(frameByteData: ByteArray) {
        super.populateFromFrameData(frameByteData)
        // Update our local message variables
        val payloadString = payloadDataString()
        setMessageDestinationCallsignFromPayload(payloadString)
        setMessageFromPayload(payloadString)
        setMessageNumberFromPayload(payloadString)
    }

    fun setMessageSourceCallsign(sourceCallsign: String) {
        setSourceCallsign(sourceCallsign)
    }

    fun setMessageDestinationCallsign(destinationCallsign: String) {
        this.messageDestinationCallsign = destinationCallsign
    }

    fun setMessage(message: String) {
        this.message = message
    }

    fun setMessageNumber(messageNumber: Int) {
        this.messageNumber = messageNumber
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

    override fun packetData(): ByteArray {
        // Construct the payload using our local variables
        val stringPayloadBuilder = StringBuilder()
        stringPayloadBuilder.append(":")
        stringPayloadBuilder.append(messageDestinationCallsign)
        stringPayloadBuilder.append(":")
        if (messageNumber >= 0) {
            stringPayloadBuilder.append("{")
            stringPayloadBuilder.append(messageNumber)
        }

        this.payloadData = stringUtils.convertStringToBytes(stringPayloadBuilder.toString())
        // Construct and return the packet data
        return super.packetData()
    }

    private fun setMessageDestinationCallsignFromPayload(payloadDataString: String) {
        var callsign = payloadDataString.split(":")[1]
        callsign = stringUtils.removeWhitespace(callsign)
        this.messageDestinationCallsign = callsign
    }

    private fun setMessageFromPayload(payloadDataString: String) {
        var parsedMessage = payloadDataString.split(":")[2]
        if (parsedMessage.contains("{")) {
            parsedMessage = parsedMessage.split("{")[0]
        }
        this.message = parsedMessage
    }

    private fun setMessageNumberFromPayload(payloadDataString: String) {
        if (payloadDataString.contains("{")) {
            var messageNumberString = payloadDataString().split("{")[1]
            messageNumberString = stringUtils.removeEOLChars(messageNumberString)
            this.messageNumber = messageNumberString.toInt()
        } else {
            this.messageNumber = -1
        }
    }

}