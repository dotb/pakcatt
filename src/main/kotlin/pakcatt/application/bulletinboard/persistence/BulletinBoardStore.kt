package pakcatt.application.bulletinboard.persistence

import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component
import java.util.*

@Component
class BulletinBoardStore(val bulletinBoardThreadRepository: BulletinBoardThreadRepository,
                         val bulletinBoardPostRepository: BulletinBoardPostRepository) {

    fun getThreads(): List<BulletinBoardThread> {
        return bulletinBoardThreadRepository.findAll().sortedBy { it.lastUpdatedDataTime }
    }

    fun getThread(threadId: Int): BulletinBoardThread? {
        val optionalThread = bulletinBoardThreadRepository.findById(threadId)
        return when (optionalThread.isPresent) {
            true -> optionalThread.get()
            false -> null
        }
    }

    fun getPostsInThread(threadId: Int): List<BulletinBoardPost> {
        return bulletinBoardPostRepository.findByThreadNumber(threadId).sortedBy { it.postDateTime }
    }

    /**
     * Fetch a post based on it's indexed position in the list of posts
     * within a topic. This method is inefficient and be improved, perhaps
     * through caching. But, it should still be faster than a 9600baud modem :)
     * When we implement an Internet facing interface it'll become more of a concern.
     */
    fun getPost(postIndex: Int, threadId: Int): BulletinBoardPost? {
        val allPosts = getPostsInThread(threadId)
        return if (allPosts.size - 1 > postIndex) {
            allPosts[postIndex]
        } else {
            null
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