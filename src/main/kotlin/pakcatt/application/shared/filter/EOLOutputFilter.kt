package pakcatt.application.shared.filter

import org.springframework.stereotype.Component
import pakcatt.application.shared.UserContext
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.filter.common.OutputFilter

/**
 * This filter uses the remote station preferences to customise the
 * end-of-line characters in the response, so that it looks great.
 */
@Component
class EOLOutputFilter: OutputFilter() {

    // Customise EOL characters to that responses are well formatted for a different types of remote terminal
    override fun applyFilter(response: AppResponse, userContext: UserContext?) {
        if (null != userContext) {
            val adjustedResponseString =
                stringUtils.fixEndOfLineCharacters(response.responseString(), userContext.eolSequence)
            response.updateResponseString(adjustedResponseString)
        }
    }

}