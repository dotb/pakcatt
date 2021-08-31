package pakcatt.application.bulletinboard.persistence

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Service

@Service
interface BulletinBoardPostRepository: MongoRepository<BulletinBoardPost, Int> {

    fun findByThreadNumber(threadNumber: Int): List<BulletinBoardPost>

}