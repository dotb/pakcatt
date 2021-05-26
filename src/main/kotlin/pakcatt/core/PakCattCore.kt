package pakcatt.core

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.sqrt

@Service
class PakCattCore(private val serviceName: String) {

    private val logger = LoggerFactory.getLogger(PakCattCore::class.java)

/*
    private fun handleInputLine(inputLine: String) {
        logger.debug("Serial handling input: $inputLine")
        if (inputLine.toLowerCase().contains("what?")) {
            logger.debug("Ignoring TNC saying What?")
        } else if (inputLine.toLowerCase().contains("maildrop")) {
            logger.debug("Ignoring Maildrop connection")
        } else if (inputLine.toLowerCase().contains("[c]")) {
            logger.info("Got a new connection: $inputLine")
        } else if (inputLine.toLowerCase().contains("*** connected")) {
            logger.info("Got a new connection")
        } else if (inputLine.toLowerCase().contains("*** disconnected")) {
            logger.info("User disconnected")
        } else if (inputLine.toLowerCase().contains("vk2vro")) {
            sendToSerialPort("Welcome to ${serviceName}! Hey Joel, I know who you are! What can I do for you?")
        } else if (inputLine.toLowerCase().contains("help")) {
            sendToSerialPort("Your options are: check mail, hello, and ping")
        } else if (inputLine.toLowerCase().contains("check mail")) {
            sendToSerialPort("You've always got mail ;-)")
        } else if (inputLine.toLowerCase().contains("check mail")) {
            sendToSerialPort("You've always got mail ;-)")
        } else if (inputLine.toLowerCase().contains("hello")) {
            sendToSerialPort("Hi, there! *wave*")
        } else if (inputLine.toLowerCase().contains("ping")) {
            sendToSerialPort("Pong!")
        } else if (inputLine.toLowerCase().contains("pong")) {
            sendToSerialPort("Ping! haha")
        } else if (inputLine.toLowerCase().contains("cmd")) {
            sendToSerialPort("converse")
        } else if (inputLine.toLowerCase().contains("sqrt")) {
            handleSQRT(inputLine)
        } else {
            sendToSerialPort("Say, what?")
        }
    }

    private fun handleSQRT(inputLine: String) {
        val arg = inputLine.split(" ")[1]
        val result = sqrt(arg.toDouble())
        sendToSerialPort("Square root of $arg is $result")
    }
*/

}