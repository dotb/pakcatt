package pakcatt.application.mailbox.persistence

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Service

@Service
interface MailMessageRepository: MongoRepository<MailMessage, Int> {

    fun findByFromCallsignOrToCallsign(fromCallsign: String, toCallsign: String): List<MailMessage>

}