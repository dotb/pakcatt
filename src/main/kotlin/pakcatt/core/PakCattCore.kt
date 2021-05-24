package pakcatt.core

import gnu.io.NRSerialPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.io.DataInputStream
import java.io.DataOutputStream
import kotlin.math.sqrt

@Service
class PakCattCore(private val serviceName: String,
                  private val serialPortPath: String,
                  private val serialPortBaud: Int) {

    private val logger = LoggerFactory.getLogger(PakCattCore::class.java)
    private val serial = NRSerialPort(serialPortPath, serialPortBaud)
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null
    private var inputLine = ""

//    @Scheduled(fixedRate = 10000)
//    private fun send() {
//        if (serialIsConnected()) {
//            logger.debug("Sending a command to serial port")
//            sendToSerialPort("help")
//        }
//    }

    @Scheduled(fixedRate = 5000)
    private fun checkSerialPortConnection() {
        if (!serialIsConnected()) {
            connectToSerialPort()
        }
    }

    @Scheduled(fixedRate = 1)
    private fun serviceSerialPort() {
        try {

            if (serialIsConnected() && serialHasInputWaiting()) {
                val char = serialReadChar()
                if (char == '\r') { // End of line
                    serialReadChar() // remove the addition EOL character
                    handleInputLine(inputLine)
                    inputLine = ""
                } else {
                    inputLine += char
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    private fun handleInputLine(inputLine: String) {
        logger.debug("Serial handling input: $inputLine")
        if (inputLine.toLowerCase().contains("what?")) {
            logger.debug("Ignoring TNC saying What?")
        } else if (inputLine.toLowerCase().contains("maildrop")) {
            logger.debug("Ignoring Maildrop connection")
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

    private fun sendToSerialPort(outputLine: String) {
        logger.debug("Serial sending line: $outputLine")
        val myOutputStream = outputStream
        if (null != myOutputStream) {
            myOutputStream.writeBytes(outputLine)
            myOutputStream.writeBytes("\n\r")
        } else {
            logger.error("Tried to send a line to the serial port but it's not connected.")
        }
    }

    private fun connectToSerialPort() {
        logger.info("Connecting to serial port $serialPortPath at $serialPortBaud baud")
        serial.connect()
        inputStream = DataInputStream(serial.inputStream)
        outputStream = DataOutputStream(serial.outputStream)
    }

    private fun disconnectFromSerialPort() {
        logger.info("Disconnecting from serial port $serialPortPath")
        serial.disconnect()
        inputStream = null
        outputStream = null
    }

    private fun serialIsConnected(): Boolean {
        return serial.isConnected
    }

    private fun serialHasInputWaiting(): Boolean {
        val myInputStream = inputStream
        return if (null != myInputStream) myInputStream.available() > 0 else false
    }


    private fun serialReadChar(): Char {
        val myInputStream = inputStream
        if (null != myInputStream) {
            val byte = myInputStream.readByte()
            return byte.toChar()
        } else {
            logger.error("Tried to read from serial port but the input stream does not exist.")
            return Char.MIN_VALUE
        }
    }

}