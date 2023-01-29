package pakcatt.application.last.persistence

import org.springframework.data.annotation.Id
import java.util.*

data class LastEntry(@Id var callsign: String,
                     var lastSeen: Date,
                     var channelId: String = "unknown") {
}