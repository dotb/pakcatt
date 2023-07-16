package pakcatt.application.shared.filter

import org.springframework.stereotype.Component
import pakcatt.application.shared.ConnectionType
import pakcatt.application.shared.filter.common.AppInputFilter
import pakcatt.application.shared.model.AppRequest

@Component
class ForwardingInputFilter: AppInputFilter() {
    override fun applyFilter(request: AppRequest) {
        val userContext = request.userContext
        if (null != userContext && request.remoteCallsign == "VK3LIT-1") {
            userContext.connectionType = ConnectionType.INTERACTIVE_FORWARDING
        }
    }
}