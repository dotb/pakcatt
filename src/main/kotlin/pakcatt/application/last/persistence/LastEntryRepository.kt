package pakcatt.application.last.persistence

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Service

@Service
interface LastEntryRepository: MongoRepository<LastEntry, String>