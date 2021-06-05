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
        // Ensure any previous connection is closed, and open a new one
        closeConnection(request.remoteCallsign, request.addressedToCallsign)
        val userContext = contextForConversation(request.remoteCallsign, request.addressedToCallsign)

        // Find an app who is willing to connect
        val connectionResponse = findAppWillingToAcceptConnection(request)

        // Update the app focus state in the user context if required.
        updateAppFocus(connectionResponse.nextApp(), userContext)
        return addPromptToResponse(userContext.engagedApplication(), connectionResponse) as ConnectionResponse
    }

    private fun findAppWillingToAcceptConnection(request: LinkRequest): ConnectionResponse {
        // Start with the default decision to ignore this connect request
        var finalConnectionDecision = ConnectionResponse.ignore()

        // Find a root application who wants to accept the connection
        for (app in rootApplications) {
            val connectionDecision = app.decisionOnConnectionRequest(request)
            when (connectionDecision.responseType) {
                ConnectionResponseType.CONNECT_WITH_MESSAGE -> finalConnectionDecision = connectionDecision
                ConnectionResponseType.CONNECT -> finalConnectionDecision = connectionDecision
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
        // Return any response with an included command prompt string
        return addPromptToResponse(userContext.engagedApplication(), interactionResponse) as InteractionResponse
    }

    private fun getResponseForReceivedMessage(request: LinkRequest, userContext: UserContext): InteractionResponse  {
        // Start with a default response to ignored the incoming request
        var finalInteractionResponse = InteractionResponse.ignore()

        // Share the request with app registered root level apps for processing
        for (app in rootApplications) {
            val interactionResponse = app.handleReceivedMessage(request)
            when (interactionResponse.responseType) {
                InteractionResponseType.SEND_TEXT -> finalInteractionResponse = interactionResponse
                InteractionResponseType.ACK_ONLY -> finalInteractionResponse = interactionResponse
                InteractionResponseType.IGNORE -> logger.trace("App isn't interested in responding.")
            }
        }

        // Check if this user is engaged with a specific app. This app response will take priority
        val app = userContext.engagedApplication()
        if (null != app) {
            // Direct requests to the app this user is engaged with
            finalInteractionResponse = app.handleReceivedMessage(request)
        }

        return finalInteractionResponse
    }

    override fun closeConnection(remoteCallsign: String, myCallsign: String) {
        val key = contextKey(remoteCallsign, myCallsign)
        currentUsers.remove(key)
    }

    /* Methods that handle the user context objects */

    // Update the app focus state in the user context if required.
    private fun updateAppFocus(nextApp: SubApp?, userContext: UserContext) {
        if (null != nextApp && nextApp is NavigateBack) {
            for (i in 1..nextApp.steps) {
                userContext.navigateBack()
            }
        } else if (null != nextApp) {
            userContext.navigateToApp(nextApp)
        }
    }

    // Rewrite the prompt string into the textual response
    private fun addPromptToResponse(app: SubApp?, response: LinkResponse): LinkResponse {
        val message = response.responseString()

        when (val prompt = app?.returnCommandPrompt()) {
            "" -> response.updateResponseString("$message\n\r")
            null -> response.updateResponseString("$message\n\r")
            else -> response.updateResponseString("$message\n\r$prompt")
        }

        return response
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