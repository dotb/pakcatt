package pakcatt.application.last.filter

import org.springframework.stereotype.Component
import pakcatt.application.shared.filter.common.AppInputFilter
import pakcatt.application.last.persistence.LastEntryStore
import pakcatt.application.shared.model.AppRequest
import java.util.*

/**
 * This filter works with the LastApp. It logs callsigns seen using the system
 * so that they can be displayed in the last log.
 */
@Component
class LastAppInputFilter(private val lastEntryStore: LastEntryStore): AppInputFilter() {

    override fun applyFilter(request: AppRequest) {
        updateCallsignEntry(request.remoteCallsign, request.channelIdentifier)
    }

    private fun updateCallsignEntry(callsign: String, channelIdentifier: String) {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(callsign)
        lastEntryStore.updateLastEntryFor(formattedCallsign, Date(), channelIdentifier)
    }

}