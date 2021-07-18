package pakcatt.application.scriptable

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.scriptable.model.Script
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
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
    override fun decisionOnConnectionRequest(request: AppRequest): AppResponse {
        for (script in scriptableScripts) {
            val connectScriptPath = script.pathConnect
            runCommand(connectScriptPath)
        }
        return AppResponse.ignore()
    }

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        return AppResponse.ignore()
    }

    private fun runCommand(scriptPath: String): String? {
        val workingDir = File(scriptWorkingDir)
        try {
            val proc = ProcessBuilder(scriptPath)
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(scriptTimeout, TimeUnit.SECONDS)
            return proc.inputStream.bufferedReader().readText()
        } catch(e: IOException) {
            e.printStackTrace()
            return null
        }
    }

}