package pakcatt.application.bulletinboard.edit

import pakcatt.application.bulletinboard.persistence.BulletinBoardPost
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.bulletinboard.persistence.BulletinBoardThread
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens

class AddThreadApp(private val newThread: BulletinBoardThread,
                   private val bulletinBoardStore: BulletinBoardStore): SubApp() {

    override fun returnCommandPrompt(): String {
        return "Topic:"
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val topic = stringUtils.removeEOLChars(request.message)
        newThread.topic = topic
        return AppResponse.sendText("Compose the first post and finish with . on a line of it's own.", AddPostApp(newThread,
                                                                                                                BulletinBoardPost(newThread.fromCallsign),
                                                                                                                bulletinBoardStore))
    }
}