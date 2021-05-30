package pakcatt.application.shared

class AppResponse(val responseType: ResponseType,
                  val message: String) {

    companion object {
        fun text(message: String): AppResponse {
            return AppResponse(ResponseType.TEXT, message)
        }

        fun none(): AppResponse {
            return AppResponse(ResponseType.NONE, "")
        }
    }

    enum class ResponseType {
        TEXT, NONE
    }

    fun responseType(): ResponseType {
        return responseType
    }

    fun message(): String {
        return message
    }

}