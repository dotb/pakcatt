package pakcatt.application.bulletinboard.persistence

import org.springframework.data.annotation.Id
import java.util.*

data class BulletinBoardThread(val fromCallsign: String,
                               val startDateTime: Date = Date(),
                               var lastUpdatedDataTime: Date = Date(),
                               var topic: String = "",
                               @Id var threadNumber: Int = 0)