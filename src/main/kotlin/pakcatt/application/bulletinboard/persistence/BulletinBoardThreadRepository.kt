package pakcatt.application.bulletinboard.persistence

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Service

@Service
interface BulletinBoardThreadRepository: MongoRepository<BulletinBoardThread, Int> {

}