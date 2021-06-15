package pakcatt.network.packet.protocol.aprs.model

import pakcatt.network.packet.kiss.model.KissFrame

class APRSMessageFrame: APRSFrame() {
    /*
    Example message from VK3LIT-7 to VK2VRO-7
    Received bytes:	 00   82 a0 b2 60 66 88 60   ac 96 66 98 92 a8 ee   ae   92   88   8a 62 40 62 ae 92 88 8a 64 40 63 03 f0 3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d
    From: VK3LI-7 to: APY03D-0 controlType: Via1: WIDE1-1 Via2: WIDE2-1 controlType: U_UNNUMBERED_INFORMATION Payload: :VK2VRO-7 :hello{47
    :  V  K  2  V  R  O  -  7  SP :  H  e  l  l  o  {  4  7  END
    3a 56 4b 32 56 52 4f 2d 37 20 3a 68 65 6c 6c 6f 7b 34 37 0d
     */

    override fun populateFromKissFrame(kissFrame: KissFrame) {
        super.populateFromKissFrame(kissFrame)
        populateFromFrameData(kissFrame.packetData())
    }

    override fun populateFromFrameData(frameByteData: ByteArray) {
        super.populateFromFrameData(frameByteData)

    }

    fun messageSourceCallsign(): String {
        return sourceCallsign()
    }

    fun messageDestinationCallsign(): String {
        var callsign = payloadDataString().split(":")[1]
        callsign = stringUtils.removeWhitespace(callsign)
        return return callsign
    }

    fun message(): String {
        var message = payloadDataString().split(":")[2]
        if (message.contains("{")) {
            message = message.split("{")[0]
        }
        return message
    }

    fun messageNumber(): Int {
        if (payloadDataString().contains("{")) {
            var messageNumberString = payloadDataString().split("{")[1]
            messageNumberString = stringUtils.removeEOLChars(messageNumberString)
            return messageNumberString.toInt()
        }
        return -1
    }

}