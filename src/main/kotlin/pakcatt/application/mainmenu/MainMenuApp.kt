package pakcatt.application.mainmenu

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.mailbox.MailboxApp
import pakcatt.application.mailbox.persistence.MailboxStore
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.ConnectionResponse
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.application.shared.RootApp
import kotlin.math.sqrt

@Component
@Profile("production")
class MainMenuApp(val myCall: String,
                  val mailboxStore: MailboxStore): RootApp() {

    private val logger = LoggerFactory.getLogger(MainMenuApp::class.java)

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
            request.message.toLowerCase().contains("help") -> {
                InteractionResponse.sendText("Your options are: mail, hello, ping, and sqrt <number>")
            }
            request.message.toLowerCase().contains("mail") -> {
                InteractionResponse.sendText("Launching mail", MailboxApp(mailboxStore))
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
                val result = handleSQRT(request.message)
                InteractionResponse.sendText(result)
            }
            request.message.toLowerCase().contains("nop") -> {
                return InteractionResponse.acknowledgeOnly()
            } else -> {
               InteractionResponse.sendText("?? Type help for a list of commands")
            }
        }
    }

    private fun handleSQRT(inputLine: String): String {
        val arg = inputLine.split(" ")[1]
        val result = sqrt(arg.toDouble()).toString()
        return "Square root of $arg is $result"
    }

}