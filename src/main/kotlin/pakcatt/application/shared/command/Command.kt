package pakcatt.application.shared.command

import pakcatt.application.shared.SubApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ResponseType

data class Command(private val fullCommand: String,
                   private var description: String? = null,
                   private var shortCut: String? = null,
                   private var response: ResponseType = ResponseType.IGNORE,
                   private var message: String? = null,
                   private var nextApp: SubApp? = null,
                   private var function: ((request: AppRequest) -> AppResponse)? = null) {

    fun description(description: String): Command {
        this.description = description
        return this
    }

    fun shortCut(shortCut: String): Command {
        this.shortCut = shortCut
        return this
    }

    fun ignore(): Command {
        this.response = ResponseType.IGNORE
        return this
    }

    fun ackOnly(): Command {
        this.response = ResponseType.ACK_ONLY
        return this
    }

    fun reply(message: String): Command {
        this.message = message
        return this
    }

    fun openApp(nextApp: SubApp?): Command {
        this.nextApp = nextApp
        return this
    }

    fun function(function: (request: AppRequest) -> AppResponse): Command {
        this.function = function
        return this
    }

    fun commandText(): String {
        return fullCommand.toLowerCase()
    }

    fun shortCutText(): String {
        return when (val myShortCutText = shortCut) {
            null -> ""
            else -> myShortCutText.toLowerCase()
        }
    }

    fun descriptionText(): String? {
        return description
    }

    fun execute(request: AppRequest): AppResponse {
        val myMessage = message
        val myNextApp = nextApp
        val myFunction = function
        return when {
            response == ResponseType.ACK_ONLY -> AppResponse.acknowledgeOnly()
            null != myMessage && null != myNextApp -> AppResponse.sendText(myMessage, nextApp)
            null != myMessage -> AppResponse.sendText(myMessage)
            null != myNextApp -> AppResponse.acknowledgeOnly(nextApp)
            null != myFunction -> myFunction(request)
            else -> AppResponse.ignore()
        }
    }

}