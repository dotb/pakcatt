package pakcatt.application.shared

class AppResponse(val responseType: ResponseType,
                  val message: String) {

    companion object {
        /**
         * Response with a textual message.
         */
        fun text(message: String): AppResponse {
            return AppResponse(ResponseType.TEXT, message)
        }

        /**
         * Acknowlege the message without a textual response.
         */
        fun acknowlegeOnly(): AppResponse {
            return AppResponse(ResponseType.ACK_ONLY, "")
        }

        /**
         * Ignore the message, and don't respond at all.
         */
        fun ignore(): AppResponse {
            return AppResponse(ResponseType.IGNORE, "")
        }
    }

    enum class ResponseType {
        TEXT, ACK_ONLY, IGNORE
    }

    fun responseType(): ResponseType {
        return responseType
    }

    fun message(): String {
        return message
    }

}