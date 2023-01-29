package pakcatt.application.shared.filter

import org.springframework.stereotype.Component
import pakcatt.application.shared.UserContext
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.filter.common.OutputFilter
import pakcatt.application.shared.FORMAT
import pakcatt.application.shared.TextFormat

@Component
class MentionOutputFilter: OutputFilter() {

    private val textFormat = TextFormat()

    override fun applyFilter(response: AppResponse, userContext: UserContext?) {

        if (null != userContext) {
            val responseString = response.responseString()
            val userCallsign = stringUtils.formatCallsignRemoveSSID(userContext.remoteCallsign)
            val mentionedCallsign = "${textFormat.format(FORMAT.BOLD)}@${userCallsign.toUpperCase()}${textFormat.format(FORMAT.RESET)}"
            val newResponseString = responseString.replace("@$userCallsign", mentionedCallsign, true)
            response.updateResponseString(newResponseString)
        }
    }

}