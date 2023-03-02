package pakcatt.application.mainmenu

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.bulletinboard.BulletinBoardApp
import pakcatt.application.bulletinboard.persistence.BulletinBoardStore
import pakcatt.application.last.LastApp
import pakcatt.application.last.persistence.LastEntryStore
import pakcatt.application.mailbox.MailboxApp
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.settings.SettingsApp
import pakcatt.application.shared.COLOUR
import pakcatt.application.shared.FORMAT
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.command.Command
import pakcatt.application.tell.TellApp
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import pakcatt.application.tell.model.TellAppConfig
import java.lang.StringBuilder
import kotlin.math.sqrt

@Component
@Profile("production")
class MainMenuApp(private val myCall: String,
                  private val mailboxStore: MailboxStore,
                  bulletinBoardStore: BulletinBoardStore,
                  lastEntryStore: LastEntryStore,
                  private val welcomeMessage: String,
                  boardPromptTopicLength: Int,
                  boardSummaryLength: Int,
                  boardPostListLength: Int,
                  private val tellAppConfig: TellAppConfig): RootApp() {

    private val lastApp = LastApp(lastEntryStore)

    init {
        // Apps and functionality
        registerCommand(Command("board")   .shortCuts(listOf("b")) .reply("Launching Bulletin Board")    .openApp(BulletinBoardApp(bulletinBoardStore,
                                                                                                boardPromptTopicLength,
                                                                                                boardSummaryLength,
                                                                                                boardPostListLength))  .description("Open the Bulletin Board"))
        registerCommand(Command("mail")    .shortCuts(listOf("m")) .reply("Launching Mail")    .openApp(MailboxApp(mailboxStore))  .description("Open your mailbox"))
        registerCommand(Command("last")    .function(::handleLast).description("last [callsign] - See when others were last seen"))
        registerCommand(Command("tell")    .function(::handleTell).description("tell <callsign> - Send a quick APRS message to someone"))
        registerCommand(Command("sqrt")    .function(::handleSQRT).description("sqrt <number> - Calculate the square root of an argument"))

        // Cute responses
        registerCommand(Command("hello")   .reply("Hi, there! *wave*").description("Just a friendly welcome :-)"))
        registerCommand(Command("ping")    .reply("Pong!").description("I'll reply with a pong"))
        registerCommand(Command("pong")    .reply("Ping! haha :P").description("I'll reply with a pong"))
        registerCommand(Command("beep")    .reply("beep! $beepChar").description("Send a beep instruction to your terminal"))

        // Terminal tests
        registerCommand(Command("bold")    .reply("${textFormat.escapeChar}[1mThis should be BOLD and${textFormat.escapeChar}[0m this should not be bold.").description("Test the bold control character on your terminal"))
        registerCommand(Command("styles")  .function(::allTheStyles).description("Test the styles supported by your terminal"))
        registerCommand(Command("nop")     .ackOnly().description("I'll do nothing, just acknowledge your request"))
        registerCommand(Command("ignore")  .ignore().description("I'll receive your command but won't acknowledge it"))
        registerCommand(Command("settings").reply("Launching Settings") .openApp(SettingsApp()).description("View your environment settings"))
    }

    override fun returnCommandPrompt(): String {
        return "menu>"
    }

    override fun decisionOnConnectionRequest(request: AppRequest): AppResponse {
        return if (isAddressedToMe(request, myCall)) {
            val stringBuilder = StringBuilder()
            val mailboxApp = MailboxApp(mailboxStore)
            val unreadMessages = mailboxApp.unreadMessageCount(request)

            stringBuilder.append(welcomeMessage)
            stringBuilder.append(stringUtils.EOL)
            if (unreadMessages > 1) {
                stringBuilder.append("You have $unreadMessages unread messages.${stringUtils.EOL}")
            } else if (unreadMessages > 0) {
                stringBuilder.append("You have an unread message.${stringUtils.EOL}")
            }
            AppResponse.sendText(stringBuilder.toString(), this)
        } else {
            AppResponse.ignore()
        }
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
       return when (isAddressedToMe(request, myCall)) {
           true -> handleRequestWithARegisteredCommand(request, parsedCommandTokens)
           else -> AppResponse.ignore()
       }
    }

    private fun handleTell(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val destinationCallsign = parsedCommandTokens.argumentAtIndexAsString(1)
        return if (destinationCallsign.isNotBlank()) {
            AppResponse.sendText("", TellApp(destinationCallsign, myCall, request.remoteCallsign, tellAppConfig.channelIdentifiers))
        } else {
            AppResponse.sendText("You need to specify a callsign")
        }
    }

    private fun handleSQRT(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val arg = parsedCommandTokens.argumentAtIndexAsString(1)
        val result = sqrt(arg.toDouble()).toString()
        return AppResponse.sendText("Square root of $arg is $result")
    }

    private fun handleLast(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return when (val arg = parsedCommandTokens.argumentAtIndexAsString(1)) {
            "" -> AppResponse.sendText(lastApp.lastEntries(request))
            else -> AppResponse.sendText(lastApp.lastEntryFor(request, arg))
        }
    }

    private fun allTheStyles(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        val returnString = StringBuilder()
        for (style in FORMAT.values()) {
            returnString.append(textFormat.format(style))
            returnString.append("Style")
            returnString.append(textFormat.format(FORMAT.RESET))
            returnString.append(stringUtils.EOL)
        }
        for (colour in COLOUR.values()) {
            returnString.append(textFormat.fgColour(colour))
            returnString.append("Foreground Colour")
            returnString.append(textFormat.fgColour(COLOUR.DEFAULT))
            returnString.append(stringUtils.EOL)
        }
        for (colour in COLOUR.values()) {
            returnString.append(textFormat.bgColour(colour))
            returnString.append("Background Colour")
            returnString.append(textFormat.bgColour(COLOUR.DEFAULT))
            returnString.append(stringUtils.EOL)
        }

        return AppResponse.sendText(returnString.toString())
    }

}