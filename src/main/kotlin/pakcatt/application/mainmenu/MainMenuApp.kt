package pakcatt.application.mainmenu

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.last.LastApp
import pakcatt.application.mailbox.MailboxApp
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.ConnectionResponse
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.application.shared.RootApp
import pakcatt.util.StringUtils
import java.lang.StringBuilder
import kotlin.math.sqrt

@Component
@Profile("production")
class MainMenuApp(val myCall: String,
                  val mailboxStore: MailboxStore,
                  val lastApp: LastApp): RootApp() {

    private val stringUtils = StringUtils()
    private val beepChar = 7.toChar()
    private val escapeChar = 27.toChar()

    override fun returnCommandPrompt(): String {
        return "menu>"
    }

    override fun decisionOnConnectionRequest(request: LinkRequest): ConnectionResponse {
        return when (isAddressedToMe(request, myCall)) {
            true -> ConnectionResponse.connectWithMessage("Welcome to PakCatt! Type help to learn more :-)", this)
            false -> ConnectionResponse.ignore()
        }
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
       return when {
            notAddressedToMe(request, myCall) -> {
               return InteractionResponse.ignore()
            }
            request.message.toLowerCase().contains("mail") -> {
                InteractionResponse.sendText("Launching mail", MailboxApp(mailboxStore))
            }
            request.message.toLowerCase().contains("last") -> {
                InteractionResponse.sendText(handleLast(request.message))
            }
            request.message.toLowerCase().contains("hello") -> {
                InteractionResponse.sendText("Hi, there! *wave*")
            }
            request.message.toLowerCase().contains("ping") -> {
                return InteractionResponse.sendText("Pong!")
            }
            request.message.toLowerCase().contains("pong") -> {
                return InteractionResponse.sendText("Ping! haha")
            }
           request.message.toLowerCase().contains("sqrt") -> {
                InteractionResponse.sendText(handleSQRT(request.message))
            }
            request.message.toLowerCase().contains("nop") -> {
                return InteractionResponse.acknowledgeOnly()
            }
            request.message.toLowerCase().contains("beep") -> {
                return InteractionResponse.sendText("beep! $beepChar")
            }
            request.message.toLowerCase().contains("bold") -> {
                return InteractionResponse.sendText("This should be $escapeChar[1mBOLD$escapeChar[0m and this, should not be bold.")
            }
            request.message.toLowerCase().contains("styles") -> {
               return InteractionResponse.sendText(allTheStyles())
            } else -> {
               InteractionResponse.sendText("Try these commands: mail, last, hello, ping, and sqrt <number>")
            }
        }
    }

    private fun handleSQRT(inputLine: String): String {
        val arg = getArgument(inputLine, "0")
        val result = sqrt(arg.toDouble()).toString()
        return "Square root of $arg is $result"
    }

    private fun handleLast(inputLine: String): String {
        return when (val arg = getArgument(inputLine, "")) {
            "" -> lastApp.lastEntries()
            else -> lastApp.lastEntryFor(arg)
        }
    }

    private fun getArgument(inputLine: String, defaultArgument: String): String {
        val stringTokens = inputLine.split(" ")
        return when (stringTokens.size) {
            2 -> stringUtils.removeEOLChars(stringTokens[1])
            else -> defaultArgument
        }
    }

    private fun allTheStyles(): String {
        val returnString = StringBuilder()
        for (style in 1..8) {
            returnString.append("$escapeChar[${style}m Style $style $escapeChar[0m\r\n")
        }
        return returnString.toString()
    }

}