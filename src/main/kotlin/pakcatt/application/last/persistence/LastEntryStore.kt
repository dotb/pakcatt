package pakcatt.application.last.persistence

import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.util.*

@Component
class LastEntryStore(private val lastEntryRepository: LastEntryRepository) {

    @Async
    fun updateLastEntryFor(callsign: String, datetimeStamp: Date) {
        var lastEntry = getLastEntry(callsign)
        if (null == lastEntry) {
            lastEntry = LastEntry(callsign, datetimeStamp)
            lastEntryRepository.insert(lastEntry)
        } else {
            lastEntry.lastSeen = datetimeStamp
            lastEntryRepository.save(lastEntry)
        }
    }

    fun getLastEntry(callsign: String): LastEntry? {
        val lastEntry = lastEntryRepository.findById(callsign)
        return when (lastEntry.isPresent) {
            true -> lastEntry.get()
            false -> null
        }
    }

    fun getLastEntries(): List<LastEntry> {
        return lastEntryRepository.findAll()
    }

}