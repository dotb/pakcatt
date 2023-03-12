package pakcatt.application.bulletinboard.read

import pakcatt.application.bulletinboard.edit.AddPostApp
import pakcatt.application.bulletinboard.persistence.BulletinBoardPost
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.bulletinboard.persistence.BulletinBoardThread
import pakcatt.application.shared.ConnectionType
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.list.LimitType
import pakcatt.application.shared.list.ListLimiter
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import pakcatt.util.ColumnFormatter
import java.lang.StringBuilder

class ReadThreadApp(private val parentThread: BulletinBoardThread,
                    private val bulletinBoardStore: BulletinBoardStore,
                    private val boardPromptTopicLength: Int,
                    private val boardSummaryLength: Int,
                    private val defaultPostListLength: Int): SubApp() {

    init {
        registerCommand(Command("list") .function(::listPosts).description("list [number of posts] - List posts specifying an optional length"))
        registerCommand(Command("open") .function(::readPost).description("Read a post"))
        registerCommand(Command("post") .function(::newPost).description("Add a post"))
        registerCommand(Command("back") .reply("") .openApp(NavigateBack(1)).description("Return to the list of topics"))
    }

    override fun returnCommandPrompt(): String {
        val topicSummary = stringUtils.shortenString(parentThread.topic, boardPromptTopicLength, false)
        return "board/${parentThread.threadNumber} ${topicSummary}>"
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return handleRequestWithARegisteredCommand(request, parsedCommandTokens)
    }

    private fun listPosts(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val requestedNumberOfPosts = parsedCommandTokens.argumentAtIndexAsInt(1)
        val numberOfPostsToList = requestedNumberOfPosts ?: defaultPostListLength
        val postList = bulletinBoardStore.getPostsInThread(parentThread.threadNumber)
        val listLimiter = ListLimiter<BulletinBoardPost>(numberOfPostsToList, LimitType.LIST_TAIL).addItems(postList)
        val responseString = compilePostListResponse(request.userContext?.connectionType == ConnectionType.INTERACTIVE_USER, listLimiter)
        return AppResponse.sendText(responseString)
    }

    private fun compilePostListResponse(channelIsInteractive: Boolean, listLimiter: ListLimiter<BulletinBoardPost>): String {
        val listResponse = StringBuilder()
        val totalPosts = listLimiter.getAllItems().size
        val columnFormatter = ColumnFormatter(2, 4, 14, 7, 6)

        if (totalPosts > 0) {
            if (channelIsInteractive) {
                listResponse.append(stringUtils.EOL)
                listResponse.append(columnFormatter.formatLineAsColumns("", "No", "Posted", "By", "Size", isBold = true))
            }
            for (postToList in listLimiter.getLimitedList()) {
                val post = postToList.item
                val postLengthStr = post.body.length.toString() + "B"
                val summary = stringUtils.shortenString(post.body, boardSummaryLength, true)
                if (channelIsInteractive) {
                    listResponse.append(columnFormatter.formatLineAsColumns("", postToList.originalIndex.toString(), stringUtils.formattedDateLong(post.postDateTime), post.fromCallsign, postLengthStr, isBold = true))
                    listResponse.append(summary)
                    listResponse.append(stringUtils.EOL)
                } else {
                    listResponse.append(postToList.originalIndex.toString(), " ", stringUtils.formattedDateShort(post.postDateTime), " ", post.fromCallsign, " ", postLengthStr, " ", stringUtils.removeEOLChars(summary, " "))
                }
                listResponse.append(stringUtils.EOL)
            }
        }
        if (channelIsInteractive) {
            listResponse.append(stringUtils.EOL)
            listResponse.append(totalPosts)
            listResponse.append(" posts in: ")
            listResponse.append(parentThread.topic)
            listResponse.append(stringUtils.EOL)
        }
        return listResponse.toString()
    }

    private fun readPost(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        var post: BulletinBoardPost? = null
        val postNumber = parsedCommandTokens.argumentAtIndexAsInt(1)
        if (null != postNumber) {
            post = bulletinBoardStore.getPost(postNumber, parentThread.threadNumber)
        }
        return if (null != post && post.threadNumber == parentThread.threadNumber) {
            AppResponse.sendText("${stringUtils.EOL}${post.body}")
        } else {
            AppResponse.sendText("Post not found")
        }
    }

    private fun newPost(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val authorCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        return AppResponse.sendText("Compose your post and finish with . on a line of it's own.", AddPostApp(parentThread,
                                                                                                            BulletinBoardPost(authorCallsign),
                                                                                                            bulletinBoardStore
        ))
    }

}