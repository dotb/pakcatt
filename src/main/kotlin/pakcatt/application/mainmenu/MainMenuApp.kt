package pakcatt.application.mainmenu

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.last.LastApp
import pakcatt.application.mailbox.MailboxApp
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.command.Command
import pakcatt.application.tell.TellApp
import pakcatt.application.shared.model.AppResponse
import java.lang.StringBuilder
import kotlin.math.sqrt

@Component
@Profile("production")
class MainMenuApp(val myCall: String,
                  val mailboxStore: MailboxStore,
                  val lastApp: LastApp): RootApp() {

    private val beepChar = 7.toChar()
    private val escapeChar = 27.toChar()

    init {
        // Apps and functionality
        registerCommand(Command("mail") .reply("Launching Mail")    .openApp(MailboxApp(mailboxStore))  .description("Open the Mail app"))
        registerCommand(Command("last") .function { handleLast(it) }.description("last [callsign] - See when others were last seen"))
        registerCommand(Command("tell") .function { handleTell(it) } .description("tell <callsign> - Send a quick APRS message to someone."))
        registerCommand(Command("sqrt") .function { handleSQRT(it) }.description("sqrt <number> - Calculate the square root of an argument"))

        // Cute responses
        registerCommand(Command("hello").reply("Hi, there! *wave*") .description("Just a friendly welcome :-)"))
        registerCommand(Command("ping") .reply("Pong!").description("I'll reply with a pong"))
        registerCommand(Command("pong") .reply("Ping! haha :P").description("I'll reply with a pong"))
        registerCommand(Command("beep") .reply("beep! $beepChar").description("Send a beep instruction to your terminal"))

        // Terminal tests
        registerCommand(Command("bold") .reply("This should be $escapeChar[1mBOLD$escapeChar[0m and this, should not be bold.").description("Test the bold control character on your terminal"))
        registerCommand(Command("styles").function { allTheStyles() }.description("Test the styles supported by your terminal"))
        registerCommand(Command("nop")  .ackOnly().description("I'll do nothing, just acknowledge your request"))
        registerCommand(Command("ignore").ignore().description("I'll receive your command but won't acknowledge it."))
    }

    override fun returnCommandPrompt(): String {
        return "menu>"
    }

    override fun decisionOnConnectionRequest(request: AppRequest): AppResponse {
        return if (isAddressedToMe(request, myCall)) {
            val stringBuilder = StringBuilder()
            val mailboxApp = MailboxApp(mailboxStore)
            val unreadMessages = mailboxApp.unreadMessageCount(request)

            stringBuilder.append("Welcome to PakCatt! Type help to learn more :-)\r\n")
            if (unreadMessages > 1) {
                stringBuilder.append("You have $unreadMessages unread messages.\r\n")
            } else if (unreadMessages > 0) {
                stringBuilder.append("You have an unread message.\r\n")
            }
            AppResponse.sendText(stringBuilder.toString(), this)
        } else {
            AppResponse.ignore()
        }
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
       return when (isAddressedToMe(request, myCall)) {
           true -> handleRequestWithRegisteredCommand(request)
           else -> AppResponse.ignore()
       }
    }

    private fun handleTell(request: AppRequest): AppResponse {
        val destinationCallsign = parseStringArgument(request.content, "")
        return if (destinationCallsign.isNotBlank()) {
            AppResponse.sendText("", TellApp(destinationCallsign, myCall, request.remoteCallsign))
        } else {
            AppResponse.sendText("You need to specify a callsign")
        }
    }

    private fun handleSQRT(request: AppRequest): AppResponse {
        val arg = parseStringArgument(request.content, "0")
        val result = sqrt(arg.toDouble()).toString()
        return AppResponse.sendText("Square root of $arg is $result")
    }

    private fun handleLast(request: AppRequest): AppResponse {
        return when (val arg = parseStringArgument(request.content, "")) {
            "" -> AppResponse.sendText(lastApp.lastEntries())
            else -> AppResponse.sendText(lastApp.lastEntryFor(arg))
        }
    }

    private fun allTheStyles(): AppResponse {
        val returnString = StringBuilder()
        for (style in 1..8) {
            returnString.append("$escapeChar[${style}m Style $style $escapeChar[0m\r\n")
        }
        return AppResponse.sendText(returnString.toString())
    }

}