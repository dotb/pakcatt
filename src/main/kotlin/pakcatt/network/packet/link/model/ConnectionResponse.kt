package pakcatt.network.packet.link.model

import pakcatt.application.shared.SubApp

enum class ConnectionResponseType {
    CONNECT, CONNECT_WITH_MESSAGE, IGNORE
}

class ConnectionResponse(val responseType: ConnectionResponseType,
                         val message: String,
                         nextApp: SubApp? = null): LinkResponse(nextApp) {

    companion object {
        /**
         * Accept the incoming connection.
         */
        fun connect(nextApp: SubApp? = null): ConnectionResponse {
            return ConnectionResponse(ConnectionResponseType.CONNECT, "", nextApp)
        }

        /**
         * Accept the connection and send a welcome message.
         */
        fun connectWithMessage(message: String, nextApp: SubApp? = null): ConnectionResponse {
            return ConnectionResponse(ConnectionResponseType.CONNECT_WITH_MESSAGE, message, nextApp)
        }

        /**
         * Ignore this incoming connection. Note, another application may accept it.
         */
        fun ignore(nextApp: SubApp? = null): ConnectionResponse {
            return ConnectionResponse(ConnectionResponseType.IGNORE, "", nextApp)
        }
    }

}