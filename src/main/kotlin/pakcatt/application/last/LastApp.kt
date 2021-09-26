package pakcatt.application.last

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.last.persistence.LastEntryStore
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.util.StringUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Keeps a record of the last time a callsign is seen on the system,
 * and allows users to query the record.
 */
@Component
@Profile("production")
class LastApp(private val lastEntryStore: LastEntryStore): RootApp() {

    private val dateFormatter = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm")

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun decisionOnConnectionRequest(request: AppRequest): AppResponse {
        updateCallsignEntry(request.remoteCallsign)
        return AppResponse.ignore()
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        updateCallsignEntry(request.remoteCallsign)
        return AppResponse.ignore()
    }

    fun lastEntryFor(callsign: String): String {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(callsign)
        return when (val lastEntry = lastEntryStore.getLastEntry(formattedCallsign)) {
            null -> "Haven't seen $formattedCallsign${stringUtils.EOL}"
            else -> "Last seen ${lastEntry.callsign} ${dateFormatter.format(lastEntry.lastSeen)}${stringUtils.EOL}"
        }
    }

    fun lastEntries(): String {
        val lastEntries = lastEntryStore.getLastEntries()
        val stringBuilder = StringBuilder()
        for (lastEntry in lastEntries) {
            stringBuilder.append("Last seen ${lastEntry.callsign} ${dateFormatter.format(lastEntry.lastSeen)}")
            stringBuilder.append(stringUtils.EOL)
        }
        return stringBuilder.toString()
    }

    private fun updateCallsignEntry(callsign: String) {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(callsign)
        lastEntryStore.updateLastEntryFor(formattedCallsign, Date())
    }
}