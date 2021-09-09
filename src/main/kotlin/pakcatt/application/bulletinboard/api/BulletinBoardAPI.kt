package pakcatt.application.bulletinboard.api

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import pakcatt.application.bulletinboard.persistence.BulletinBoardPost
import pakcatt.application.bulletinboard.persistence.BulletinBoardPostRepository
import pakcatt.application.bulletinboard.persistence.BulletinBoardThread
import pakcatt.application.bulletinboard.persistence.BulletinBoardThreadRepository

@RestController
class BulletinBoardAPI(var bulletinBoardThreadRepository: BulletinBoardThreadRepository,
                       var bulletinBoardPostRepository: BulletinBoardPostRepository) {

    @GetMapping("/app/board/topic")
    fun getTopics(): List<BulletinBoardThread> {
        return bulletinBoardThreadRepository.findAll()
    }

    @GetMapping("/app/board/topic/{id}")
    fun getTopic(@PathVariable("id") id: Int): BulletinBoardThread {
        val topic = bulletinBoardThreadRepository.findById(id)
        return if (topic.isPresent) {
            topic.get()
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    @GetMapping("/app/board/topic/{id}/post")
    fun getPosts(@PathVariable("id") id: Int): List<BulletinBoardPost> {
        return bulletinBoardPostRepository.findByThreadNumber(id)
    }

    @GetMapping("/app/board/topic/{topicId}/post/{postId}")
    fun getPost(@PathVariable("topicId") topicId: Int, @PathVariable("postId") postId: Int): BulletinBoardPost {
        val postList = bulletinBoardPostRepository.findByThreadNumber(topicId)
        return if (postList.size > postId) {
            postList[postId]
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

}