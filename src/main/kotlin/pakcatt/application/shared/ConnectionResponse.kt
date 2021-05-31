package pakcatt.application.shared

class ConnectionResponse(val responseType: ConnectionResponseType,
                         val message: String) {

    enum class ConnectionResponseType {
        CONNECT, CONNECT_WITH_MESSAGE, IGNORE
    }

    companion object {
        /**
         * Accept the incoming connection.
         */
        fun connect(): ConnectionResponse {
            return ConnectionResponse(ConnectionResponseType.CONNECT, "")
        }

        /**
         * Accept the connection and send a welcome message.
         */
        fun connectWithMessage(message: String):ConnectionResponse {
            return ConnectionResponse(ConnectionResponseType.CONNECT_WITH_MESSAGE, message)
        }

        /**
         * Ignore this incoming connection. Note, another application may accept it.
         */
        fun ignore(): ConnectionResponse {
            return ConnectionResponse(ConnectionResponseType.IGNORE, "")
        }
    }

}