package pakcatt.application.bulletinboard.persistence

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.*

@Component
class BulletinBoardStore(val bulletinBoardThreadRepository: BulletinBoardThreadRepository,
                         val bulletinBoardPostRepository: BulletinBoardPostRepository) {

    fun getThreads(): List<BulletinBoardThread> {
        return bulletinBoardThreadRepository.findAll()
    }

    fun getThread(threadId: Int): BulletinBoardThread? {
        val optionalThread = bulletinBoardThreadRepository.findById(threadId)
        return when (optionalThread.isPresent) {
            true -> optionalThread.get()
            false -> null
        }
    }

    fun getPostsInThread(threadId: Int): List<BulletinBoardPost> {
        return bulletinBoardPostRepository.findByThreadNumber(threadId)
    }

    fun getPost(postId: Int): BulletinBoardPost? {
        val optionalPost =  bulletinBoardPostRepository.findById(postId)
        return when (optionalPost.isPresent) {
            true -> optionalPost.get()
            false -> null
        }
    }

    fun addPostToThread(newPost: BulletinBoardPost, parentThread: BulletinBoardThread) {
        // Check if the thread is new, too, and persist it first if it is
        val persistedThread = if (0 == parentThread.threadNumber) {
            storeNewThread(parentThread)
        } else {
            parentThread
        }
        newPost.threadNumber = persistedThread.threadNumber
        persistedThread.lastUpdatedDataTime = Date()
        bulletinBoardThreadRepository.save(persistedThread)
        storeNewPost(newPost)
    }

    private fun storeNewThread(newThread: BulletinBoardThread): BulletinBoardThread {
        var nextThreadNumber = 1
        val allThreads = bulletinBoardThreadRepository.findAll(Sort.by(Sort.Direction.DESC, "threadNumber"))
        if (allThreads.size > 0) {
            val lastThread = allThreads.first()
            nextThreadNumber = lastThread.threadNumber + 1
        }
        newThread.threadNumber = nextThreadNumber
        return bulletinBoardThreadRepository.insert(newThread)
    }

    private fun storeNewPost(newPost: BulletinBoardPost): BulletinBoardPost {
        var nextPostNumber = 1
        val allPosts = bulletinBoardPostRepository.findAll(Sort.by(Sort.Direction.DESC, "postNumber"))
        if (allPosts.size > 0) {
            val lastPost = allPosts.first()
            nextPostNumber = lastPost.postNumber + 1
        }
        newPost.postNumber = nextPostNumber
        return bulletinBoardPostRepository.insert(newPost)
    }

}