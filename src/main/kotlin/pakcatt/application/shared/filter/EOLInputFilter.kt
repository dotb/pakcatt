package pakcatt.application.shared.filter

import org.springframework.stereotype.Component
import pakcatt.application.shared.UserContext
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.filter.common.InputFilter

/**
 * This filter does two things
 *
 * 1) It inspects the end-of-line characters sent from remote
 * stations and stores an EOL preference in the users' context.
 *
 * 2) Then it cleans up the EOL characters sent from remote
 * stations, re-writing the request with a pre-configured consistent
 * EOL sequence so that the database stores content with a consistent
 * approach to EOL, and, within the PakCatt system a consistent EOL approach
 * can be assumed. Although, with EOL one should never assume! :-)
 */
@Component
class EOLInputFilter: InputFilter() {

    override fun applyFilter(request: AppRequest) {
        updateUserContextWithAutodetectedEOLSequence(request.message, request.userContext)
        cleanEOLCharacters(request)
    }

    // Updated the user context with their preferred EOL characters, based on what they have sent us.
    private fun updateUserContextWithAutodetectedEOLSequence(requestString: String, userContext: UserContext?) {
        if (null != userContext) {
            testForEOLSequenceAndUpdateUserContext(requestString, userContext, "\r")
            testForEOLSequenceAndUpdateUserContext(requestString, userContext, "\n")
            testForEOLSequenceAndUpdateUserContext(requestString, userContext, "\r\n")
            testForEOLSequenceAndUpdateUserContext(requestString, userContext, "\n\r")
        }
    }

    private fun testForEOLSequenceAndUpdateUserContext(requestString: String, userContext: UserContext, eolSequence: String) {
        if (requestString.contains(eolSequence)) {
            userContext.eolSequence = eolSequence
        }
    }

    private fun cleanEOLCharacters(request: AppRequest) {
        // Re-write incoming EOL sequences to a configured standard
        val cleanedMessage = stringUtils.fixEndOfLineCharacters(request.message, stringUtils.EOL)
        request.message = cleanedMessage
    }

}