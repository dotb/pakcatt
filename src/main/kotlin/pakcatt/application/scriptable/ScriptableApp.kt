package pakcatt.application.scriptable

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.scriptable.model.Script
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import pakcatt.application.shared.model.ResponseType
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * The Scriptable app provides an interface to external software, for example
 * command-line shell or python scripts.
 */

enum class ScriptType {
    CONNECT, REQUEST
}

@Component
@Profile("production")
class ScriptableApp(private val scriptableScripts: List<Script>,
                    private val scriptWorkingDir: String,
                    private val scriptTimeout: Long): RootApp() {

    val logger: Logger = LoggerFactory.getLogger(ScriptableApp::class.java)

    /**
     * Find a script that will accept this connection.
     * Only one script can accept and handle a connection.
     */
    override fun decisionOnConnectionRequest(request: AppRequest): AppResponse {
        return handleRequestWithScript(ScriptType.CONNECT, request)
    }

    /**
     * Scripts don't support a prompt. A prompt can be injected into the response
     * by the script, based in any state the script is keeping.
     */
    override fun returnCommandPrompt(): String {
        return ""
    }

    /**
     * Find a script that will handle this message.
     * Only one script can handle the message. The first non-ignored response will be selected.
     */
    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return handleRequestWithScript(ScriptType.REQUEST, request)
    }

    private fun handleRequestWithScript(scriptType: ScriptType, appRequest: AppRequest): AppResponse {
        for (script in scriptableScripts) {
            val connectScriptPath = when (scriptType) {
                ScriptType.CONNECT -> script.pathConnect
                ScriptType.REQUEST -> script.pathRequest
            }
            val responseString = getOutputFromScript(connectScriptPath, appRequest)
            val scriptResponse = parseAppResponse(responseString)
            if (scriptResponse.responseType != ResponseType.IGNORE) {
                return scriptResponse
            }
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
                "IGNORE" -> {
                    // Log any debug text after the IGNORE instruction
                    if (chompedString.contains(" ")) {
                        val startLog = chompedString.indexOf(" ") + 1
                        logger.debug("Script logged: {}", chompedString.substring(startLog, chompedString.length))
                    }
                    AppResponse.ignore()
                }
                else -> {
                    logger.error("Script returned an invalid response type: $chompedString")
                    AppResponse.ignore()
                }
            }
        } else {
            logger.error("Script returned an invalid response: $chompedString")
            AppResponse.ignore()
        }
    }

    private fun getOutputFromScript(scriptPath: String, appRequest: AppRequest): String {
        val workingDir = File(scriptWorkingDir)
        return try {
            val objectMapper = ObjectMapper()
            val jsonInputArguments = objectMapper.writeValueAsString(appRequest)
            val proc = ProcessBuilder(scriptPath, jsonInputArguments)
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            proc.waitFor(scriptTimeout, TimeUnit.SECONDS)
            val error = proc.errorStream.bufferedReader().readText()
            if (error.isNotEmpty()) {
                logger.error("While executing script: {} error: {}", scriptPath, error)
            }
            proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            logger.error("Exception while executing script {} {}", scriptPath, e.localizedMessage)
            ""
        }
    }

}