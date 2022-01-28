package pakcatt.application.bulletinboard

import pakcatt.application.bulletinboard.edit.AddThreadApp
import pakcatt.application.bulletinboard.read.ReadThreadApp
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.bulletinboard.persistence.BulletinBoardThread
import pakcatt.application.shared.FORMAT
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import java.lang.StringBuilder

class BulletinBoardApp(private val bulletinBoardStore: BulletinBoardStore,
                       private val boardPromptTopicLength: Int,
                       private val boardSummaryLength: Int,
                       private val boardPostListLength: Int): SubApp() {

    init {
        registerCommand(Command("list") .function (::listThreads).description("List the threads"))
        registerCommand(Command("open") .function (::openThread).description("Open a thread"))
        registerCommand(Command("post") .function (::postNewThread).description("Post a new thread"))
        registerCommand(Command("back") .reply("Bye").shortCuts(listOf("quit", "exit")).openApp(NavigateBack(1)).description("Return to the main menu"))
    }

    override fun returnCommandPrompt(): String {
        return "board>"
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return handleRequestWithARegisteredCommand(request, parsedCommandTokens)
    }

    private fun listThreads(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val threadList = bulletinBoardStore.getThreads()
        val listResponse = StringBuilder()
        val threadCount = threadList.size

        if (threadCount > 0) {
            if (request.channelIsInteractive) {
                listResponse.append(stringUtils.EOL)
                listResponse.append(textFormat.format(FORMAT.BOLD))
                listResponse.append("  No  ${tabSpace}Updated     ${tabSpace}Topic")
                listResponse.append(textFormat.format(FORMAT.RESET))
                listResponse.append(stringUtils.EOL)
            }
            for (thread in threadList) {
                val topicSummary = stringUtils.shortenString(thread.topic, boardSummaryLength, true)
                if (request.channelIsInteractive) {
                    listResponse.append("  ")
                }
                listResponse.append(thread.threadNumber)
                if (request.channelIsInteractive) {
                    listResponse.append("  ")
                    listResponse.append(tabSpace)
                    listResponse.append(stringUtils.formattedDateLong(thread.lastUpdatedDataTime))
                    listResponse.append(tabSpace)
                } else {
                    listResponse.append(" ")
                    listResponse.append(stringUtils.formattedDateShort(thread.lastUpdatedDataTime))
                    listResponse.append(": ")
                }
                listResponse.append(topicSummary)
                listResponse.append(stringUtils.EOL)
            }
        }
        if (request.channelIsInteractive) {
            listResponse.append(threadCount)
            listResponse.append(" threads")
            listResponse.append(stringUtils.EOL)
        }
        return AppResponse.sendText(listResponse.toString())
    }

    private fun openThread(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val threadNumber = parsedCommandTokens.argumentAtIndexAsInt(1)
        var returnedThread: BulletinBoardThread? = null
        if (null != threadNumber) {
            returnedThread = bulletinBoardStore.getThread(threadNumber)
        }
        return if (null != returnedThread) {
            AppResponse.sendText("", ReadThreadApp(returnedThread, bulletinBoardStore, boardPromptTopicLength, boardSummaryLength, boardPostListLength))
        } else {
            AppResponse.sendText("No thread found")
        }
    }

    private fun postNewThread(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val authorCallsign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        return AppResponse.sendText("", AddThreadApp(BulletinBoardThread(authorCallsign), bulletinBoardStore))
    }

}