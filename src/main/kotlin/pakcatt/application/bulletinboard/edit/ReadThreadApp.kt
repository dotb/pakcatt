package pakcatt.application.bulletinboard.edit

import pakcatt.application.bulletinboard.persistence.BulletinBoardPost
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.bulletinboard.persistence.BulletinBoardThread
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.FORMAT
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.util.StringUtils
import java.lang.StringBuilder

class ReadThreadApp(private val parentThread: BulletinBoardThread,
                    private val bulletinBoardStore: BulletinBoardStore,
                    private val boardPromptTopicLength: Int,
                    private val boardSummaryLength: Int,
                    private val defaultPostListLength: Int): SubApp() {

    init {
        registerCommand(Command("list") .function { listPosts(it) }  .description("list [number of posts] - List posts specifying an optional length"))
        registerCommand(Command("read") .function { readPost(it) }   .description("Read a post."))
        registerCommand(Command("post") .function { newPost(it) }   .description("Add a post"))
        registerCommand(Command("back") .reply("") .openApp(NavigateBack(1)).description("Return to the list of topics"))
    }

    override fun returnCommandPrompt(): String {
        val topicSummary = "${stringUtils.shortenString(parentThread.topic, boardPromptTopicLength, false)}"
        return "board/${parentThread.threadNumber} ${topicSummary}>"
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        return handleRequestWithRegisteredCommand(request)
    }

    private fun listPosts(request: AppRequest): AppResponse {
        val postList = bulletinBoardStore.getPostsInThread(parentThread.threadNumber)
        val totalPosts = postList.size
        val requestedNumberOfPosts = parseIntArgument(request.message)
        val numberOfPostsToList = requestedNumberOfPosts ?: defaultPostListLength
        val rangeOfIndexesToInclude = totalPosts - numberOfPostsToList..totalPosts
        val responseString = compilePostListResponse(postList, totalPosts, rangeOfIndexesToInclude)
        return AppResponse.sendText(responseString)
    }

    private fun compilePostListResponse(postList: List<BulletinBoardPost>, totalPosts: Int, rangeOfIndexesToInclude: IntRange): String {
        val listResponse = StringBuilder()
        if (totalPosts > 0) {
            listResponse.append(StringUtils.EOL)
            listResponse.append(format(FORMAT.BOLD))
            listResponse.append("No${tabSpace}Posted       By${tabSpace}${tabSpace}Size")
            listResponse.append(format(FORMAT.RESET))
            listResponse.append(StringUtils.EOL)
            for ((index, post) in postList.withIndex()) {
                if (rangeOfIndexesToInclude.contains(index)) {
                    val summary = "${stringUtils.shortenString(post.body, boardSummaryLength, true)}"
                    listResponse.append(StringUtils.EOL)
                    listResponse.append(format(FORMAT.BOLD))
                    listResponse.append(index)
                    listResponse.append(")")
                    listResponse.append(tabSpace)
                    listResponse.append(stringUtils.formattedDate(post.postDateTime))
                    listResponse.append("  ")
                    listResponse.append(post.fromCallsign)
                    listResponse.append(tabSpace)
                    listResponse.append(post.body.length)
                    listResponse.append("B")
                    listResponse.append(format(FORMAT.RESET))
                    listResponse.append(StringUtils.EOL)
                    listResponse.append(summary)
                    listResponse.append(StringUtils.EOL)
                    listResponse.append(StringUtils.EOL)
                }
            }
        }
        listResponse.append(StringUtils.EOL)
        listResponse.append(totalPosts)
        listResponse.append(" posts in: ")
        listResponse.append(parentThread.topic)
        listResponse.append(StringUtils.EOL)
        return listResponse.toString()
    }

    private fun readPost(request: AppRequest): AppResponse {
        var post: BulletinBoardPost? = null
        val postNumber = parseIntArgument(request.message)
        if (null != postNumber) {
            post = bulletinBoardStore.getPost(postNumber, parentThread.threadNumber)
        }
        return if (null != post && post.threadNumber == parentThread.threadNumber) {
            AppResponse.sendText("${StringUtils.EOL}${post.body}")
        } else {
            AppResponse.sendText("Post not found")
        }
    }

    private fun newPost(request: AppRequest): AppResponse {
        val authorCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        return AppResponse.sendText("Compose your post and finish with . on a line of it's own.", AddPostApp(parentThread,
                                                                                                            BulletinBoardPost(authorCallsign),
                                                                                                            bulletinBoardStore
        ))
    }

}