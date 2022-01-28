package pakcatt.application.bulletinboard.edit

import pakcatt.application.bulletinboard.persistence.BulletinBoardPost
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.bulletinboard.persistence.BulletinBoardThread
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens

class AddPostApp(private val parentThread: BulletinBoardThread,
                 private val newPost: BulletinBoardPost,
                 private val bulletinBoardStore: BulletinBoardStore): SubApp() {

    private var composedBody = StringBuilder()

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val bodyText = request.message
        return if (stringUtils.removeEOLChars(bodyText) == ".") {
            // New topics need 2 steps back, while posts to an existing topic need one step back.
            val stepsBack = when (parentThread.threadNumber) {
                0 -> 2
                else -> 1
            }
            newPost.body = composedBody.toString()
            bulletinBoardStore.addPostToThread(newPost, parentThread)
            AppResponse.sendText("Thanks. Your post has been stored.", NavigateBack(stepsBack))
        } else {
            composedBody.append(bodyText)
            AppResponse.acknowledgeOnly()
        }
    }
}