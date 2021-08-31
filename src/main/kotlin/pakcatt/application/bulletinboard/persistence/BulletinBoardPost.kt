package pakcatt.application.bulletinboard.persistence

import org.springframework.data.annotation.Id
import java.util.*

data class BulletinBoardPost(val fromCallsign: String,
                             val postDateTime: Date = Date(),
                             var body: String = "",
                             var threadNumber: Int = 0,
                             @Id var postNumber: Int = 0)