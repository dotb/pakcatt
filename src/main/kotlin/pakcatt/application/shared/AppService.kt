package pakcatt.application.shared

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.ConnectionResponse
import pakcatt.network.packet.link.model.InteractionResponse

interface AppInterface {
    fun getDecisionOnConnectionRequest(request: LinkRequest): ConnectionResponse
    fun getResponseForReceivedMessage(request: LinkRequest): InteractionResponse
    fun closeConnection(remoteCallsign: String, myCallsign: String)
}

@Service
class AppService(val applications: List<PakCattApp>): AppInterface {

    private val logger = LoggerFactory.getLogger(AppService::class.java)
    private var currentUsers = HashMap<String, UserContext>()

    /* AppInterface methods delegated from the LinkService */
    override fun getDecisionOnConnectionRequest(request: LinkRequest): ConnectionResponse {
        var finalConnectionDecision = ConnectionResponse.ignore()
        for (app in applications) {
            val connectionDecision = app.decisionOnConnectionRequest(request)
            when (connectionDecision.responseType) {
                // Return with the commitment to connect with a welcome message
                ConnectionResponse.ConnectionResponseType.CONNECT_WITH_MESSAGE -> return connectionDecision
                // Follow through with the connection, but see if another app has a welcome message
                ConnectionResponse.ConnectionResponseType.CONNECT -> finalConnectionDecision = connectionDecision
                // Do nothing, this app wants to ignore this connection
                ConnectionResponse.ConnectionResponseType.IGNORE -> logger.trace("App isn't interested in this connection.")
            }
        }
        return finalConnectionDecision
    }

    override fun getResponseForReceivedMessage(request: LinkRequest): InteractionResponse {
        var finalInteractionResponse = InteractionResponse.ignore()
        for (app in applications) {
            val interactionResponse = app.handleReceivedMessage(request)
            when (interactionResponse.responseType) {
                // If an app wants to ACK and send a message, return right away
                InteractionResponse.InteractionResponseType.SEND_TEXT -> return interactionResponse
                // If an app wants to ACK, continue to search for an app that wants to return a message, too
                InteractionResponse.InteractionResponseType.ACK_ONLY -> finalInteractionResponse = interactionResponse
                // Do nothing, this app doesn't want to handle this request
                InteractionResponse.InteractionResponseType.IGNORE -> logger.trace("App isn't interested in responding.")
            }
        }
        return finalInteractionResponse
    }

    override fun closeConnection(remoteCallsign: String, myCallsign: String) {
        val key = contextKey(remoteCallsign, myCallsign)
        currentUsers.remove(key)
    }

    /* Methods that handle the user context objects */
    private fun contextForConversation(remoteCallsign: String, myCallsign: String): UserContext {
        val key = contextKey(remoteCallsign, myCallsign)
        val context = currentUsers[key]
        return if (null != context) {
            context
        } else {
            val context = UserContext()
            currentUsers[key] = context
            context
        }
    }

    private fun contextKey(fromCallsign: String, myCallsign: String): String {
        return "$fromCallsign $myCallsign"
    }

}