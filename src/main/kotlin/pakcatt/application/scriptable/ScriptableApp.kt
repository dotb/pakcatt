package pakcatt.application.scriptable

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.scriptable.model.Script
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ResponseType
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * The Scriptable app provides an interface to external software, for example
 * command-line shell or python scripts.
 */
@Component
@Profile("production")
class ScriptableApp(private val scriptableScripts: List<Script>,
                    private val scriptWorkingDir: String,
                    private val scriptTimeout: Long): RootApp() {

    val logger: Logger = LoggerFactory.getLogger(ScriptableApp::class.java)

    override fun decisionOnConnectionRequest(request: AppRequest): AppResponse {
        for (script in scriptableScripts) {
            val connectScriptPath = script.pathConnect
            val responseString = getOutputFromScript(connectScriptPath, request.addressedToCallsign, request.remoteCallsign, request.message)
            val scriptResponse = parseAppResponse(responseString)
            if (scriptResponse.responseType != ResponseType.IGNORE) {
                return scriptResponse
            }
        }
        return AppResponse.ignore()
    }

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        for (script in scriptableScripts) {
            val connectScriptPath = script.pathRequest
            getOutputFromScript(connectScriptPath, request.addressedToCallsign, request.remoteCallsign, request.message)
        }
        return AppResponse.ignore()
    }

    private fun parseAppResponse(responseString: String): AppResponse {
        val chompedString = stringUtils.removeEOLChars(responseString)
        val parameters = chompedString.split(" ")
        return if (parameters.isNotEmpty()) {
            val responseTypeString = parameters[0]
            return when (responseTypeString) {
                "ACK_WITH_TEXT" -> AppResponse.sendText(chompedString.substring(14))
                    "ACK_ONLY" -> AppResponse.acknowledgeOnly()
                    "IGNORE" -> AppResponse.ignore()
                else -> {
                    logger.error("Script returned an invalid response type: $responseTypeString")
                    AppResponse.ignore()
                }
            }
        } else {
            logger.error("Script returned an invalid response: $chompedString")
            AppResponse.ignore()
        }
    }

    private fun getOutputFromScript(scriptPath: String, addressedToCallsign: String, remoteCallsign: String, message: String): String {
        val workingDir = File(scriptWorkingDir)
        return try {
            val proc = ProcessBuilder(scriptPath, addressedToCallsign, remoteCallsign, message)
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            proc.waitFor(scriptTimeout, TimeUnit.SECONDS)
            proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            logger.error(e.localizedMessage)
            ""
        }
    }

}