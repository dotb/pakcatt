package pakcatt.application.simpletest

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import pakcatt.application.shared.AppResponse
import pakcatt.application.shared.PacCattApp
import kotlin.math.sqrt

@Component
class SimpleTestApp: PacCattApp() {
    private val logger = LoggerFactory.getLogger(SimpleTestApp::class.java)

    override fun handleReceivedMessage(receivedMessage: String): AppResponse {
       return when {
            receivedMessage.toLowerCase().contains("help") -> {
                AppResponse.text("Your options are: check mail, hello, and ping")
            }
            receivedMessage.toLowerCase().contains("check mail") -> {
                AppResponse.text("You've always got mail ;-)")
            }
            receivedMessage.toLowerCase().contains("hello") -> {
                AppResponse.text("Hi, there! *wave*")
            }
            receivedMessage.toLowerCase().contains("ping") -> {
                return AppResponse.text("Pong!")
            }
            receivedMessage.toLowerCase().contains("pong") -> {
                return AppResponse.text("Ping! haha")
            }
            receivedMessage.toLowerCase().contains("cmd") -> {
                return AppResponse.text("converse")
            }
            receivedMessage.toLowerCase().contains("sqrt") -> {
                val result = handleSQRT(receivedMessage)
                AppResponse.text(result)
            }
            receivedMessage.toLowerCase().contains("nop") -> {
                return AppResponse.none()
            }
            else -> {
                return AppResponse.text("Say, what?")
            }
        }
    }

    private fun handleSQRT(inputLine: String): String {
        val arg = inputLine.split(" ")[1]
        val result = sqrt(arg.toDouble()).toString()
        return "Square root of $arg is $result"
    }

}