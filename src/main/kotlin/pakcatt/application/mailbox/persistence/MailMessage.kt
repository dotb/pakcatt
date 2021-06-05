package pakcatt.application.mailbox.persistence

import org.springframework.data.annotation.Id
import java.lang.StringBuilder
import java.util.*

data class MailMessage(val fromCallsign: String,
                       var toCallsign: String = "",
                       val dateTime: Date = Date(),
                       var subject: String = "",
                       var body: String = "",
                       @Id var messageNumber: Int = 0)