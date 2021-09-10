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

    /**
     * List all topics
     */
    @GetMapping("/app/board/topic")
    fun getTopics(): List<BulletinBoardThread> {
        return bulletinBoardThreadRepository.findAll()
    }

    /**
     * Fetch a topic based on it's database id
     * @param topicId the database id of the topic
     */
    @GetMapping("/app/board/topic/{topicId}")
    fun getTopic(@PathVariable("topicId") topicId: Int): BulletinBoardThread {
        val topic = bulletinBoardThreadRepository.findById(topicId)
        return if (topic.isPresent) {
            topic.get()
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    /**
     * List the posts within a topic
     * @param topicId database id of the topic
     */
    @GetMapping("/app/board/topic/{topicId}/post")
    fun getPosts(@PathVariable("topicId") topicId: Int): List<BulletinBoardPost> {
        return bulletinBoardPostRepository.findByThreadNumber(topicId)
    }

    /**
     * Returns a post within a topic.
     * @param topicId The database ID of the topic
     * @param postNumber the number, in order, of the post as it's listed under the topic
     */
    @GetMapping("/app/board/topic/{topicId}/post/{postNumber}")
    fun getPostWithinTopicByOrder(@PathVariable("topicId") topicId: Int, @PathVariable("postNumber") postNumber: Int): BulletinBoardPost {

        val postList = bulletinBoardPostRepository.findByThreadNumber(topicId)
        return if (postList.size > postNumber) {
            postList[postNumber]
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

    /**
     * Returns a post based on the database ID of the post
     * @param postId The database id of the post
     */
    @GetMapping("/app/board/post/{postId}")
    fun getPostById(@PathVariable("postId") postId: Int): BulletinBoardPost {
        val post = bulletinBoardPostRepository.findById(postId)
        return if (post.isPresent) {
            post.get()
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND)
        }
    }

}