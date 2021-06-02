package pakcatt.network.packet.link.model

import pakcatt.application.shared.SubApp

enum class InteractionResponseType {
    SEND_TEXT, ACK_ONLY, IGNORE
}

class InteractionResponse(val responseType: InteractionResponseType,
                          val message: String,
                          nextApp: SubApp? = null): LinkResponse(nextApp) {

    companion object {
        /**
         * Response with a textual message.
         */
        fun sendText(message: String, nextApp: SubApp? = null): InteractionResponse {
            return InteractionResponse(InteractionResponseType.SEND_TEXT, message, nextApp)
        }

        /**
         * Acknowledge the message without a textual response.
         */
        fun acknowledgeOnly(nextApp: SubApp? = null): InteractionResponse {
            return InteractionResponse(InteractionResponseType.ACK_ONLY, "", nextApp)
        }

        /**
         * Ignore the message, and don't respond at all.
         */
        fun ignore(nextApp: SubApp? = null): InteractionResponse {
            return InteractionResponse(InteractionResponseType.IGNORE, "", nextApp)
        }
    }

}