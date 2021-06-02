package pakcatt.network.packet.link.model

class InteractionResponse(val responseType: InteractionResponseType,
                          val message: String) {

    enum class InteractionResponseType {
        SEND_TEXT, ACK_ONLY, IGNORE
    }

    companion object {
        /**
         * Response with a textual message.
         */
        fun sendText(message: String): InteractionResponse {
            return InteractionResponse(InteractionResponseType.SEND_TEXT, message)
        }

        /**
         * Acknowlege the message without a textual response.
         */
        fun acknowlegeOnly(): InteractionResponse {
            return InteractionResponse(InteractionResponseType.ACK_ONLY, "")
        }

        /**
         * Ignore the message, and don't respond at all.
         */
        fun ignore(): InteractionResponse {
            return InteractionResponse(InteractionResponseType.IGNORE, "")
        }
    }

}