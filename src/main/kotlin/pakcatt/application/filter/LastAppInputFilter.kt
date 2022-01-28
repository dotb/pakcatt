package pakcatt.application.filter

import org.springframework.stereotype.Component
import pakcatt.application.filter.shared.InputFilter
import pakcatt.application.last.persistence.LastEntryStore
import pakcatt.application.shared.model.AppRequest
import java.util.*

/**
 * This filter works with the LastApp. It logs callsigns seen using the system
 * so that they can be displayed in the last log.
 */
@Component
class LastAppInputFilter(private val lastEntryStore: LastEntryStore): InputFilter() {

    override fun applyFilter(request: AppRequest) {
        updateCallsignEntry(request.remoteCallsign)
    }

    private fun updateCallsignEntry(callsign: String) {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(callsign)
        lastEntryStore.updateLastEntryFor(formattedCallsign, Date())
    }

}