package pakcatt.application.shared.model

import pakcatt.application.shared.SubApp

enum class ResponseType {
    ACK_WITH_TEXT, ACK_ONLY, IGNORE
}

class AppResponse(val responseType: ResponseType,
                  private var responseString: String,
                  private val nextApp: SubApp? = null) {

    companion object {
        /**
         * Response with a textual message.
         */
        fun sendText(message: String, nextApp: SubApp? = null): AppResponse {
            return AppResponse(ResponseType.ACK_WITH_TEXT, message, nextApp)
        }

        /**
         * Acknowledge the message without a textual response.
         */
        fun acknowledgeOnly(nextApp: SubApp? = null): AppResponse {
            return AppResponse(ResponseType.ACK_ONLY, "", nextApp)
        }

        /**
         * Ignore the message, and don't respond at all.
         */
        fun ignore(nextApp: SubApp? = null): AppResponse {
            return AppResponse(ResponseType.IGNORE, "", nextApp)
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

    override fun toString(): String {
        return "responseType: $responseType nextApp: $nextApp responseString: $responseString"
    }

}