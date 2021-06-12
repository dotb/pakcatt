package pakcatt.application.last

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.last.persistence.LastEntryStore
import pakcatt.application.shared.RootApp
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.LinkResponse
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
    private val stringUtils = StringUtils()

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun decisionOnConnectionRequest(request: LinkRequest): LinkResponse {
        updateCallsignEntry(request.remoteCallsign)
        return LinkResponse.ignore()
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        updateCallsignEntry(request.remoteCallsign)
        return LinkResponse.ignore()
    }

    fun lastEntryFor(callsign: String): String {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(callsign)
        return when (val lastEntry = lastEntryStore.getLastEntry(formattedCallsign)) {
            null -> "Haven't seen $formattedCallsign"
            else -> "Last seen ${lastEntry.callsign} ${dateFormatter.format(lastEntry.lastSeen)}"
        }
    }

    fun lastEntries(): String {
        val lastEntries = lastEntryStore.getLastEntries()
        val stringBuilder = StringBuilder()
        for (lastEntry in lastEntries) {
            stringBuilder.append("Last seen ${lastEntry.callsign} ${dateFormatter.format(lastEntry.lastSeen)}")
            stringBuilder.append("\r\n")
        }
        return stringBuilder.toString()
    }

    private fun updateCallsignEntry(callsign: String) {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(callsign)
        lastEntryStore.updateLastEntryFor(formattedCallsign, Date())
    }
}