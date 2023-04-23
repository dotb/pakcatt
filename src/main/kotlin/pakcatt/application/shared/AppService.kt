package pakcatt.application.shared

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import pakcatt.application.shared.model.*
import pakcatt.application.shared.filter.common.AppInputFilter
import pakcatt.application.shared.filter.common.AppOutputFilter
import pakcatt.util.StringUtils

interface AppInterface {
    fun getDecisionOnConnectionRequest(request: AppRequest): AppResponse
    fun getResponseForReceivedMessage(request: AppRequest): AppResponse
    fun getAdhocResponses(forDeliveryType: DeliveryType): List<AdhocMessage>
    fun closeConnection(remoteCallsign: String, myCallsign: String)
}

@Service
class AppService(private val rootApplications: List<RootApp>,
                 private val appInputFilters: List<AppInputFilter>,
                 private val appOutputFilters: List<AppOutputFilter>): AppInterface {

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
        prepareResponse(request, response)
        return response
    }

    // We've received data from a client, share it with listening apps and get a response for the client
    override fun getResponseForReceivedMessage(request: AppRequest): AppResponse {
        prepareRequest(request)
        // Get the interaction response from the app
        val response = getResponseFromApplication(request)
        prepareResponse(request, response)
        return response
    }

    private fun prepareRequest(request: AppRequest) {
        // Get this user's context
        setUserContextOnRequest(request)
        // Filter the request on input to clean up the request
        filterRequestOnInput(request)
        // Re-write incoming EOL sequences to a configured standard
        var cleanedRequest = request
        cleanedRequest.message = stringUtils.fixEndOfLineCharacters(cleanedRequest.message, stringUtils.EOL)
        // Get the interaction response from the app
    }

    private fun prepareResponse(request: AppRequest, response: AppResponse) {
        // Update any focus state in the user context if required, returned by the selected app.
        updateAppFocus(response.nextApp(), request.userContext)
        // Return any response with an included command prompt string
        addPromptToResponse(request, response)
        // Clean and customise the response before sending it to the remote party
        filterResponseOnOutput(response, request.userContext)
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

    /**
     * Iterate the registered applications to service a user request,
     * take action and return an appropriate response to the user.
     */
    private fun getResponseFromApplication(request: AppRequest): AppResponse  {
        // If the command is in dot notation, ignore any currently engaged app
        return if (stringUtils.stringIsInDottedNotation(request.message) || null == request.userContext?.engagedApplication()) {
            getResponseFromRootApplication(request)
        } else {
            // if this user is engaged with a specific app, get the response directly from the engaged app
            getResponseFromUserEngagedApplication(request, AppResponse.ignore())
        }
    }

    /**
     *  Iteratively call sub apps until the instructions issued in the
     *  command line have all been parsed and satisfied.
     */
    private fun getResponseFromRootApplication(request: AppRequest): AppResponse {
        var finalInteractionResponse = AppResponse.ignore()
        for (app in rootApplications) {
            val parsedCommandInput = ParsedCommandTokens().parseCommandLine(request.message)
            val interactionResponse = callAppsRecursively(request, parsedCommandInput, app)
            when (interactionResponse.responseType) {
                ResponseType.ACK_WITH_TEXT -> {
                    logger.trace("App responded to our request: {} {}", app.javaClass, interactionResponse)
                    finalInteractionResponse = interactionResponse
                }
                ResponseType.ACK_ONLY -> {
                    logger.trace("App responded to our request: {} {}", app.javaClass, interactionResponse)
                    finalInteractionResponse = interactionResponse
                }
                ResponseType.IGNORE -> logger.trace("App isn't interested in responding {}", app)
            }
        }
        return finalInteractionResponse
    }

    private fun callAppsRecursively(request: AppRequest, parsedCommandInput: ParsedCommandTokens, app: SubApp): AppResponse {
        var interactionResponse = app.handleReceivedMessage(request, parsedCommandInput)
        val nextApp = interactionResponse.nextApp()
        if (null != nextApp && parsedCommandInput.remainingCommandLine().isNotEmpty()) {
            val nextAppRequest = request.copy()
            nextAppRequest.message = parsedCommandInput.remainingCommandLine()
            parsedCommandInput.parseCommandLine(nextAppRequest.message)
            interactionResponse = callAppsRecursively(nextAppRequest, parsedCommandInput, nextApp)
        }
        return interactionResponse
    }

    private fun getResponseFromUserEngagedApplication(request: AppRequest, currentBestResponse: AppResponse): AppResponse {
        val currentUserEngagedApp = request.userContext?.engagedApplication()
        val parsedCommandInput = ParsedCommandTokens().parseCommandLine(request.message)
        return currentUserEngagedApp?.handleReceivedMessage(request, parsedCommandInput) ?: currentBestResponse
    }

    private fun filterRequestOnInput(request: AppRequest) {
        for (inputFilter in appInputFilters) {
            inputFilter.applyFilter(request)
        }
    }

    private fun filterResponseOnOutput(response: AppResponse, userContext: UserContext?) {
        for (outputFilter in appOutputFilters) {
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
    private fun addPromptToResponse(request: AppRequest, response: AppResponse) {
        // Only add a prompt to synchronous conversations
        if (request.channelIsInteractive) {
            val message = response.responseString()
            val currentUserEngagedApp = request.userContext?.engagedApplication()
            when (val prompt = currentUserEngagedApp?.returnCommandPrompt()) {
                "" -> response.updateResponseString("$message${stringUtils.EOL}")
                null -> response.updateResponseString("$message${stringUtils.EOL}")
                else -> response.updateResponseString("$message${stringUtils.EOL}$prompt ")
            }
        }
    }

    private fun setUserContextOnRequest(request: AppRequest): UserContext {
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