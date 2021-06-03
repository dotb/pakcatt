package pakcatt.application.shared

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.network.packet.link.model.*

interface AppInterface {
    fun getDecisionOnConnectionRequest(request: LinkRequest): ConnectionResponse
    fun getResponseForReceivedMessage(request: LinkRequest): InteractionResponse
    fun closeConnection(remoteCallsign: String, myCallsign: String)
}

@Service
class AppService(val rootApplications: List<RootApp>): AppInterface {

    private val logger = LoggerFactory.getLogger(AppService::class.java)
    private var currentUsers = HashMap<String, UserContext>()

    /* AppInterface methods delegated from the LinkService */
    override fun getDecisionOnConnectionRequest(request: LinkRequest): ConnectionResponse {
        val userContext = contextForConversation(request.remoteCallsign, request.addressedToCallsign)

        // Start with the decision to ignore
        var finalConnectionDecision = ConnectionResponse.ignore()

        for (app in rootApplications) {
            val connectionDecision = app.decisionOnConnectionRequest(request)

            // Update the app focus state in the user context if required.
            updateAppFocus(connectionDecision.nextApp(), userContext)

            // Handle the connection response from the app
            when (connectionDecision.responseType) {
                // Return with the commitment to connect with a welcome message
                ConnectionResponseType.CONNECT_WITH_MESSAGE -> return connectionDecision
                // Follow through with the connection, but see if another app has a welcome message
                ConnectionResponseType.CONNECT -> finalConnectionDecision = connectionDecision
                // Do nothing, this app wants to ignore this connection
                ConnectionResponseType.IGNORE -> logger.trace("App isn't interested in this connection.")
            }
        }
        return finalConnectionDecision
    }

    override fun getResponseForReceivedMessage(request: LinkRequest): InteractionResponse {
        // Get this user's context
        val userContext = contextForConversation(request.remoteCallsign, request.addressedToCallsign)
        // Get the interaction response from the app
        val interactionResponse = getResponseForReceivedMessage(request, userContext)
        // Update any focus state in the user context if required, returned by the selected app.
        updateAppFocus(interactionResponse.nextApp(), userContext)
        return interactionResponse
    }

    private fun getResponseForReceivedMessage(request: LinkRequest, userContext: UserContext): InteractionResponse  {
        // Check if this user is engaged with a specific app
        val app = userContext.engagedApplication()
        if (null != app) {
            // Direct requests to the app this user is engaged with
            return app.handleReceivedMessage(request)
        } else {
            // if the user is not engaged with an app, send the request to all apps
            var finalInteractionResponse = InteractionResponse.ignore()
            for (app in rootApplications) {
                val interactionResponse = app.handleReceivedMessage(request)
                when (interactionResponse.responseType) {
                    // If an app wants to ACK and send a message, return right away
                    InteractionResponseType.SEND_TEXT -> return interactionResponse
                    // If an app wants to ACK, continue to search for an app that wants to return a message, too
                    InteractionResponseType.ACK_ONLY -> finalInteractionResponse = interactionResponse
                    // Do nothing, this app doesn't want to handle this request
                    InteractionResponseType.IGNORE -> logger.trace("App isn't interested in responding.")
                }
            }
            return finalInteractionResponse
        }
    }

    override fun closeConnection(remoteCallsign: String, myCallsign: String) {
        val key = contextKey(remoteCallsign, myCallsign)
        currentUsers.remove(key)
    }

    /* Methods that handle the user context objects */

    // Update the app focus state in the user context if required.
    private fun updateAppFocus(nextApp: SubApp?, userContext: UserContext) {
        if (null != nextApp && nextApp is NavigateBack) {
            userContext.navigateBack()
        } else if (null != nextApp) {
            userContext.navigateToApp(nextApp)
        }
    }

    private fun contextForConversation(remoteCallsign: String, myCallsign: String): UserContext {
        val key = contextKey(remoteCallsign, myCallsign)
        val context = currentUsers[key]
        return if (null != context) {
            context
        } else {
            val context = UserContext(remoteCallsign, myCallsign)
            currentUsers[key] = context
            context
        }
    }

    private fun contextKey(fromCallsign: String, myCallsign: String): String {
        return "$fromCallsign $myCallsign"
    }

}