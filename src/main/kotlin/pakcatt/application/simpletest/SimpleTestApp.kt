package pakcatt.application.simpletest

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.shared.AppRequest
import pakcatt.application.shared.ConnectionResponse
import pakcatt.application.shared.InteractionResponse
import pakcatt.application.shared.PakCattApp
import kotlin.math.sqrt

@Component
@Profile("production")
class SimpleTestApp(val myCall: String): PakCattApp() {
    private val logger = LoggerFactory.getLogger(SimpleTestApp::class.java)


    override fun decisionOnConnectionRequest(request: AppRequest): ConnectionResponse {
        return when (isAddressedToMe(request, myCall)) {
            true -> ConnectionResponse.connectWithMessage("Welcome to PakCatt! Type help to learn more :-)")
            false -> ConnectionResponse.ignore()
        }
    }

    override fun handleReceivedMessage(request: AppRequest): InteractionResponse {
       return when {
            notAddressedToMe(request, myCall) -> {
               return InteractionResponse.ignore()
            }
            request.message.toLowerCase().contains("help") -> {
                InteractionResponse.sendText("Your options are: check send, list, read, delete, hello, ping, and sqrt")
            }
            request.message.toLowerCase().contains("check mail") -> {
                InteractionResponse.sendText("You've always got mail ;-)")
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
                return InteractionResponse.acknowlegeOnly()
            } else -> {
                InteractionResponse.ignore()
            }
        }
    }

    private fun handleSQRT(inputLine: String): String {
        val arg = inputLine.split(" ")[1]
        val result = sqrt(arg.toDouble()).toString()
        return "Square root of $arg is $result"
    }

}