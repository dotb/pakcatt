package pakcatt.application.bulletinboard.edit

import pakcatt.application.bulletinboard.persistence.BulletinBoardPost
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.bulletinboard.persistence.BulletinBoardThread
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.util.StringUtils
import java.lang.StringBuilder

class ReadThreadApp(private val parentThread: BulletinBoardThread,
                    private val bulletinBoardStore: BulletinBoardStore): SubApp() {

    init {
        registerCommand(Command("list") .function { listPosts(it) }  .description("List posts"))
        registerCommand(Command("read") .function { readPost(it) }   .description("Read a post"))
        registerCommand(Command("post") .function { newPost(it) }   .description("Add a post"))
        registerCommand(Command("back") .openApp(NavigateBack(1)).description("Return to the list of topics"))
    }

    override fun returnCommandPrompt(): String {
        return "${parentThread.threadNumber} ${parentThread.topic}>"
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        return handleRequestWithRegisteredCommand(request)
    }

    private fun listPosts(request: AppRequest): AppResponse {
        val postList = bulletinBoardStore.getPostsInThread(parentThread.threadNumber)
        val listResponse = StringBuilder()
        val postCount = postList.size

        if (postCount > 0) {
            listResponse.append(StringUtils.EOL)
            listResponse.append("No${tabSpace}Updated         By${tabSpace}Topic${StringUtils.EOL}")
            for (post in postList) {
                val summaryLength = 20.coerceAtMost(post.body.length)
                val summary = post.body.substring(0..summaryLength)
                listResponse.append(post.postNumber)
                listResponse.append(tabSpace)
                listResponse.append(stringUtils.formattedDate(post.postDateTime))
                listResponse.append("  ")
                listResponse.append(post.fromCallsign)
                listResponse.append(tabSpace)
                listResponse.append(summary)
                listResponse.append(StringUtils.EOL)
            }
        }
        listResponse.append(postCount)
        listResponse.append(" posts")
        listResponse.append(StringUtils.EOL)
        return AppResponse.sendText(listResponse.toString())
    }

    private fun readPost(request: AppRequest): AppResponse {
        var post: BulletinBoardPost? = null
        val postNumber = parseIntArgument(request.message)
        if (null != postNumber) {
            post = bulletinBoardStore.getPost(postNumber)
        }
        return if (null != post && post.threadNumber == parentThread.threadNumber) {
            AppResponse.sendText("${StringUtils.EOL}${post.body}")
        } else {
            AppResponse.sendText("No post found")
        }
    }

    private fun newPost(request: AppRequest): AppResponse {
        return AppResponse.sendText("Compose your post and finish with . on a line of it's own.", AddPostApp(parentThread,
                                                                                                            BulletinBoardPost(request.remoteCallsign),
                                                                                                            bulletinBoardStore
        ))
    }

}