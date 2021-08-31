package pakcatt.application.bulletinboard.edit

import pakcatt.application.bulletinboard.persistence.BulletinBoardPost
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.bulletinboard.persistence.BulletinBoardThread
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse

class AddPostApp(private val parentThread: BulletinBoardThread,
                 private val newPost: BulletinBoardPost,
                 private val bulletinBoardStore: BulletinBoardStore): SubApp() {

    private var composedBody = StringBuilder()

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        val bodyText = request.message
        return if (stringUtils.removeEOLChars(bodyText) == ".") {
            newPost.body = composedBody.toString()
            bulletinBoardStore.addPostToThread(newPost, parentThread)
            AppResponse.sendText("Thanks. Your post has been stored.", NavigateBack(2))
        } else {
            composedBody.append(bodyText)
            AppResponse.acknowledgeOnly()
        }
    }
}