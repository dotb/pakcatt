package pakcatt.network.packet.link.model

import pakcatt.application.shared.SubApp

enum class ResponseType {
    ACK_WITH_TEXT, ACK_ONLY, IGNORE
}

class LinkResponse(val responseType: ResponseType,
                   private var responseString: String,
                    private val nextApp: SubApp? = null) {

    companion object {
        /**
         * Response with a textual message.
         */
        fun sendText(message: String, nextApp: SubApp? = null): LinkResponse {
            return LinkResponse(ResponseType.ACK_WITH_TEXT, message, nextApp)
        }

        /**
         * Acknowledge the message without a textual response.
         */
        fun acknowledgeOnly(nextApp: SubApp? = null): LinkResponse {
            return LinkResponse(ResponseType.ACK_ONLY, "", nextApp)
        }

        /**
         * Ignore the message, and don't respond at all.
         */
        fun ignore(nextApp: SubApp? = null): LinkResponse {
            return LinkResponse(ResponseType.IGNORE, "", nextApp)
        }
    }


    fun nextApp(): SubApp? {
        return nextApp
    }

    fun responseString(): String {
        return responseString
    }

    fun updateResponseString(responseText: String) {
        responseString = responseText
    }

}