package pakcatt.application.shared

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.network.packet.protocol.no_layer_three.model.*
import pakcatt.util.StringUtils

interface AppInterface {
    fun getDecisionOnConnectionRequest(request: LinkRequest): LinkResponse
    fun getResponseForReceivedMessage(request: LinkRequest): LinkResponse
    fun getAdhocResponsesForDelivery(): List<LinkAdhoc>
    fun closeConnection(remoteCallsign: String, myCallsign: String)
}

@Service
class AppService(val rootApplications: List<RootApp>): AppInterface {

    private val logger = LoggerFactory.getLogger(AppService::class.java)
    private val stringUtils = StringUtils()
    private var currentUsers = HashMap<String, UserContext>()

    /* AppInterface methods delegated from the LinkService */
    override fun getDecisionOnConnectionRequest(request: LinkRequest): LinkResponse {
        // Ensure any previous connection is closed, and open a new one
        closeConnection(request.remoteCallsign, request.addressedToCallsign)
        val userContext = contextForConversation(request.remoteCallsign, request.addressedToCallsign)

        // Find an app who is willing to connect
        val connectionResponse = findAppWillingToAcceptConnection(request)

        // Update the app focus state in the user context if required.
        updateAppFocus(connectionResponse.nextApp(), userContext)
        return addPromptToResponse(userContext.engagedApplication(), connectionResponse) as LinkResponse
    }

    private fun findAppWillingToAcceptConnection(request: LinkRequest): LinkResponse {
        // Start with the default decision to ignore this connect request
        var finalConnectionDecision = LinkResponse.ignore()

        // Find a root application who wants to accept the connection
        for (app in rootApplications) {
            val connectionDecision = app.decisionOnConnectionRequest(request)
            when (connectionDecision.responseType) {
                ResponseType.ACK_WITH_TEXT -> finalConnectionDecision = connectionDecision
                ResponseType.ACK_ONLY -> finalConnectionDecision = connectionDecision
                ResponseType.IGNORE -> logger.trace("App isn't interested in this connection.")
            }
        }
        return finalConnectionDecision
    }

    // We've received data from a client, share it with listening apps and get a response for the client
    override fun getResponseForReceivedMessage(request: LinkRequest): LinkResponse {
        // Sometimes a TNC will send is only \r, we rewrite these to \n\r
        val cleanedRequest = LinkRequest(request.remoteCallsign, request.addressedToCallsign, stringUtils.fixEndOfLineCharacters(request.message))
        // Get this user's context
        val userContext = contextForConversation(cleanedRequest.remoteCallsign, cleanedRequest.addressedToCallsign)
        // Get the interaction response from the app
        val interactionResponse = getResponseForReceivedMessage(cleanedRequest, userContext)
        // Update any focus state in the user context if required, returned by the selected app.
        updateAppFocus(interactionResponse.nextApp(), userContext)
        // Return any response with an included command prompt string
        return addPromptToResponse(userContext.engagedApplication(), interactionResponse)
    }

    override fun getAdhocResponsesForDelivery(): List<LinkAdhoc> {
        val allAdhocResponses = ArrayList<LinkAdhoc>()
        for (app in rootApplications) {
            allAdhocResponses.addAll(app.flushAdhocResponses())
        }
        return allAdhocResponses
    }

    private fun getResponseForReceivedMessage(request: LinkRequest, userContext: UserContext): LinkResponse  {
        // Start with a default response to ignored the incoming request
        var finalInteractionResponse = LinkResponse.ignore()

        // Share the request with app registered root level apps for processing
        for (app in rootApplications) {
            val interactionResponse = app.handleReceivedMessage(request)
            when (interactionResponse.responseType) {
                ResponseType.ACK_WITH_TEXT -> finalInteractionResponse = interactionResponse
                ResponseType.ACK_ONLY -> finalInteractionResponse = interactionResponse
                ResponseType.IGNORE -> logger.trace("App isn't interested in responding.")
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
            "" -> response.updateResponseString("$message${StringUtils.EOL}")
            null -> response.updateResponseString("$message${StringUtils.EOL}")
            else -> response.updateResponseString("$message${StringUtils.EOL}$prompt ")
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