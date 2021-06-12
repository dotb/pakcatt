package pakcatt.application.mainmenu

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.last.LastApp
import pakcatt.application.mailbox.MailboxApp
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.command.Command
import pakcatt.network.packet.link.model.LinkResponse
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
        registerCommand(Command("last") .function { handleLast(it) }.description("See when others were last seen"))
        registerCommand(Command("sqrt") .function { handleSQRT(it) }.description("Calculate the square root of an argument"))

        // Cute responses
        registerCommand(Command("hello").reply("Hi, there! *wave*") .description("Just a friendly welcome :-)"))
        registerCommand(Command("ping") .reply("Pong!").description("I'll reply with a pong"))
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

    override fun decisionOnConnectionRequest(request: LinkRequest): LinkResponse {
        return if (isAddressedToMe(request, myCall)) {
            val stringBuilder = StringBuilder()
            val mailboxApp = MailboxApp(mailboxStore)
            val unreadMessages = mailboxApp.unreadMessageCount(request)

            stringBuilder.append("Welcome to PakCatt! Type help to learn more :-)")
            if (unreadMessages > 1) {
                stringBuilder.append("You have $unreadMessages unread messages.")
            } else if (unreadMessages > 0) {
                stringBuilder.append("You have an unread message.")
            }
            stringBuilder.append("\r\n")
            LinkResponse.sendText(stringBuilder.toString(), this)
        } else {
            LinkResponse.ignore()
        }
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
       return when (isAddressedToMe(request, myCall)) {
           true -> handleRequestWithRegisteredCommand(request)
           else -> LinkResponse.ignore()
       }
    }

    private fun handleSQRT(request: LinkRequest): LinkResponse {
        val arg = parseStringArgument(request.message, "0")
        val result = sqrt(arg.toDouble()).toString()
        return LinkResponse.sendText("Square root of $arg is $result")
    }

    private fun handleLast(request: LinkRequest): LinkResponse {
        return when (val arg = parseStringArgument(request.message, "")) {
            "" -> LinkResponse.sendText(lastApp.lastEntries())
            else -> LinkResponse.sendText(lastApp.lastEntryFor(arg))
        }
    }

    private fun allTheStyles(): LinkResponse {
        val returnString = StringBuilder()
        for (style in 1..8) {
            returnString.append("$escapeChar[${style}m Style $style $escapeChar[0m\r\n")
        }
        return LinkResponse.sendText(returnString.toString())
    }

}