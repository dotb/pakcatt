package pakcatt.application.shared

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.model.*
import pakcatt.application.filter.shared.InputFilter
import pakcatt.application.filter.shared.OutputFilter
import pakcatt.util.StringUtils

interface AppInterface {
    fun getDecisionOnConnectionRequest(request: AppRequest): AppResponse
    fun getResponseForReceivedMessage(request: AppRequest): AppResponse
    fun getAdhocResponses(forDeliveryType: DeliveryType): List<AdhocMessage>
    fun closeConnection(remoteCallsign: String, myCallsign: String)
}

@Service
class AppService(private val rootApplications: List<RootApp>,
                 private val inputFilters: List<InputFilter>,
                 private val outputFilters: List<OutputFilter>): AppInterface {

    private val logger = LoggerFactory.getLogger(AppService::class.java)
    private val stringUtils = StringUtils()
    private var currentUsers = HashMap<String, UserContext>()

    /* AppInterface methods delegated from the LinkService */
    override fun getDecisionOnConnectionRequest(request: AppRequest): AppResponse {
        // Ensure any previous connection is closed, and open a new one
        closeConnection(request.remoteCallsign, request.addressedToCallsign)
        prepareRequest(request)
        // Find an app who is willing to connect
        val response = findAppWillingToAcceptConnection(request)
        prepareResponse(response, request.userContext)
        return response
    }

    // We've received data from a client, share it with listening apps and get a response for the client
    override fun getResponseForReceivedMessage(request: AppRequest): AppResponse {
        prepareRequest(request)
        // Get the interaction response from the app
        val response = getResponseFromApplication(request)
        prepareResponse(response, request.userContext)
        return response
    }

    private fun prepareRequest(request: AppRequest) {
        // Get this user's context
        val userContext = getContextForConversation(request)
        // Filter the request on input to clean up the request
        filterRequestOnInput(request)
        // Re-write incoming EOL sequences to a configured standard
        var cleanedRequest = request
        cleanedRequest.message = stringUtils.fixEndOfLineCharacters(cleanedRequest.message, stringUtils.EOL)
        // Get the interaction response from the app
    }

    private fun prepareResponse(response: AppResponse, userContext: UserContext?) {
        // Update any focus state in the user context if required, returned by the selected app.
        updateAppFocus(response.nextApp(), userContext)
        // Return any response with an included command prompt string
        addPromptToResponse(userContext?.engagedApplication(), response)
        // Clean and customise the response before sending it to the remote party
        filterResponseOnOutput(response, userContext)
    }

    private fun findAppWillingToAcceptConnection(request: AppRequest): AppResponse {
        // Start with the default decision to ignore this connect request
        var finalConnectionDecision = AppResponse.ignore()

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

    override fun getAdhocResponses(forDeliveryType: DeliveryType): List<AdhocMessage> {
        val allAdhocResponses = ArrayList<AdhocMessage>()
        for (app in rootApplications) {
            allAdhocResponses.addAll(app.flushAdhocResponses(forDeliveryType))
        }
        return allAdhocResponses
    }

    private fun getResponseFromApplication(request: AppRequest): AppResponse  {
        // Start with a default response to ignored the incoming request
        var finalInteractionResponse = AppResponse.ignore()

        val userContext = request.userContext
        // Share the request with app registered root level apps for processing
        for (app in rootApplications) {
            val interactionResponse = app.handleReceivedMessage(request)
            when (interactionResponse.responseType) {
                ResponseType.ACK_WITH_TEXT -> finalInteractionResponse = interactionResponse
                ResponseType.ACK_ONLY -> finalInteractionResponse = interactionResponse
                ResponseType.IGNORE -> logger.trace("App isn't interested in responding {}", app)
            }
        }

        // Check if this user is engaged with a specific app. This app response will take priority
        val app = userContext?.engagedApplication()
        if (null != app) {
            // Direct requests to the app this user is engaged with
            finalInteractionResponse = app.handleReceivedMessage(request)
        }

        return finalInteractionResponse
    }

    private fun filterRequestOnInput(request: AppRequest) {
        for (inputFilter in inputFilters) {
            inputFilter.applyFilter(request)
        }
    }

    private fun filterResponseOnOutput(response: AppResponse, userContext: UserContext?) {
        for (outputFilter in outputFilters) {
            outputFilter.applyFilter(response, userContext)
        }
    }

    override fun closeConnection(remoteCallsign: String, myCallsign: String) {
        val key = contextKey(remoteCallsign, myCallsign)
        currentUsers.remove(key)
    }

    /* Methods that handle the user context objects */
    // Update the app focus state in the user context if required.
    private fun updateAppFocus(nextApp: SubApp?, userContext: UserContext?) {
        if (null != nextApp && null != userContext && nextApp is NavigateBack) {
            for (i in 1..nextApp.steps) {
                userContext.navigateBack()
            }
        } else if (null != nextApp && null != userContext) {
            nextApp.setParentRootApp(userContext.rootApplication())
            userContext.navigateToApp(nextApp)
        }
    }

    // Rewrite the prompt string into the textual response
    private fun addPromptToResponse(app: SubApp?, response: AppResponse) {
        val message = response.responseString()

        when (val prompt = app?.returnCommandPrompt()) {
            "" -> response.updateResponseString("$message${stringUtils.EOL}")
            null -> response.updateResponseString("$message${stringUtils.EOL}")
            else -> response.updateResponseString("$message${stringUtils.EOL}$prompt ")
        }
    }

    private fun getContextForConversation(request: AppRequest): UserContext {
        val remoteCallsign = request.remoteCallsign
        val myCallsign = request.addressedToCallsign
        val key = contextKey(remoteCallsign, myCallsign)
        val existingContext = currentUsers[key]
        val returnedContext = if (null != existingContext) {
            existingContext
        } else {
            val newContext = UserContext(remoteCallsign, myCallsign)
            currentUsers[key] = newContext
            newContext
        }
        request.userContext = returnedContext
        return returnedContext
    }

    private fun contextKey(fromCallsign: String, myCallsign: String): String {
        return "$fromCallsign $myCallsign"
    }

}