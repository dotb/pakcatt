package pakcatt.application.filter

import org.springframework.stereotype.Component
import pakcatt.application.shared.UserContext
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.filter.shared.OutputFilter

@Component
class MentionOutputFilter: OutputFilter() {
    override fun applyFilter(response: AppResponse, userContext: UserContext?) {

    }
}