package pakcatt.application.last

import pakcatt.application.last.persistence.LastEntryStore
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import java.text.SimpleDateFormat
import java.util.*

/**
 * Keeps a record of the last time a callsign is seen on the system,
 * and allows users to query the record.
 */
class LastApp(private val lastEntryStore: LastEntryStore): SubApp() {

    private val dateFormatterLong = SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm") // For sync terminals
    private val dateFormatterShort = SimpleDateFormat("dd-MM-yyyy@HH:mm") // For small async message devices

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return AppResponse.ignore()
    }

    fun lastEntryFor(request: AppRequest, callsign: String): String {
        val formattedCallsign = stringUtils.formatCallsignRemoveSSID(callsign)
        return when (val lastEntry = lastEntryStore.getLastEntry(formattedCallsign)) {
            null -> "Haven't seen $formattedCallsign${stringUtils.EOL}"
            else -> "${lastEntry.callsign}: ${appropriateDateFormatter(request).format(lastEntry.lastSeen)} via ${lastEntry.channelId}${stringUtils.EOL}"
        }
    }

    fun lastEntries(request: AppRequest): String {
        val lastEntries = lastEntryStore.getLastEntries()
        val stringBuilder = StringBuilder()
        for (lastEntry in lastEntries) {
            stringBuilder.append("${lastEntry.callsign}: ${appropriateDateFormatter(request).format(lastEntry.lastSeen)}")
            stringBuilder.append(" via ")
            stringBuilder.append(lastEntry.channelId)
            stringBuilder.append(stringUtils.EOL)
        }
        return stringBuilder.toString()
    }

    /**
     * We shorten the date format for smaller message based devices
     */
    private fun appropriateDateFormatter(request: AppRequest): SimpleDateFormat {
        return when (request.channelIsInteractive) {
            true -> dateFormatterLong
            false -> dateFormatterShort
        }
    }
}