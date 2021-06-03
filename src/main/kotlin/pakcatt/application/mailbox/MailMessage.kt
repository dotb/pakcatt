package pakcatt.application.mailbox

import java.lang.StringBuilder
import java.util.*

data class MailMessage(val fromCallsign: String,
                       var toCallsign: String = "",
                       val dateTime: Calendar = Calendar.getInstance(),
                       var subject: String = "",
                       val body: StringBuilder = StringBuilder()
) {
}