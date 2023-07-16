package pakcatt.network.tcp

import org.slf4j.LoggerFactory
import pakcatt.application.shared.AppService
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.ResponseType
import pakcatt.util.StringUtils
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket

class TCPClientConnection(private val clientSocket: Socket,
                          private val appService: AppService,
                          private val myCall: String,
                          private val regexForCallsignValidation: String,
                          private val preWelcomeMessage: String,
                          private val callsignRegexFailMessage: String,
                          private val stringUtils: StringUtils): Runnable {

    private val logger = LoggerFactory.getLogger(TCPClientConnection::class.java)
    private val bufferedInputReader = BufferedInputStream(clientSocket.getInputStream()).bufferedReader(Charsets.UTF_8)
    private val bufferedOutputWriter = BufferedOutputStream(clientSocket.getOutputStream()).bufferedWriter()
    private val callsignValidationRegex = regexForCallsignValidation.toRegex()
    private var remoteCall = ""

    private fun serviceNextClientRequest() {
        try {
            val inputLine = readLine()
            val appRequest = newAppRequest(inputLine)
            val appResponse = appService.getResponseForReceivedMessage(appRequest)
            sendString(appResponse.responseString())
            logger.trace("Interactive TCP session {} @ {} handled input: {}", remoteCall, clientSocket.remoteSocketAddress, stringUtils.removeEOLChars(appRequest.message))
        } catch (e: Exception) {
            disconnect(e.message)
        }
    }

    private fun getRemoteCallsign() {
        sendLine(preWelcomeMessage)
        val suppliedCallsign = stringUtils.formatCallsignRemoveSSID(readLine())
        if (callsignValidationRegex.matches(suppliedCallsign)) {
            remoteCall = suppliedCallsign
        } else {
            logger.error("Remote TCP/IP user failed callsign validation: {}", suppliedCallsign)
            sendLine(callsignRegexFailMessage)
            disconnect()
        }
    }

    private fun startApplicationConnection() {
        val connectionDecision = appService.getDecisionOnConnectionRequest(newAppRequest(""))
        when (connectionDecision.responseType) {
            ResponseType.ACK_WITH_TEXT -> sendString(connectionDecision.responseString())
            ResponseType.ACK_ONLY -> sendString("")
            ResponseType.IGNORE -> disconnect()
            ResponseType.DISCONNECT -> logger.error("NEED TO IMPLEMENT") //TODO
        }
    }

    private fun sendString(string: String) {
        bufferedOutputWriter.write(string)
        bufferedOutputWriter.flush()
    }

    private fun sendLine(line: String) {
        sendString(line + stringUtils.EOL)
    }

    private fun readLine(): String {
        val inputLine = bufferedInputReader.readLine()
        return if (null == inputLine) {
            clientSocket.close()
            ""
        } else {
            stringUtils.removeEOLChars(inputLine)
        }
    }

    private fun disconnect(reason: String? = "Unknown reason") {
        logger.info("Closing interactive TCP connection with client {}. {}.", remoteCall, reason)
        clientSocket.close()
    }

    /**
     * Prepare a new app request. Messages from traditional radio TNCs come with an EOL
     * character, whereas EOL characters are stripped off of messages received through
     * a TCP connection. So, we add a EOL character to the received messages because the
     * rest of the stack expects it.
     */
    private fun newAppRequest(inputLine: String): AppRequest {
        val inputLineWithEOL = stringUtils.fixEndOfLineCharacters(inputLine, stringUtils.EOL)
        return AppRequest("TCPClient",
            stringUtils.formatCallsignEnsureSSID(remoteCall),
            stringUtils.formatCallsignRemoveSSID(remoteCall),
            myCall,
            inputLineWithEOL)
    }

    override fun run() {
        getRemoteCallsign()
        logger.info("Starting interactive TCP connection for {} @ {}", remoteCall, clientSocket.remoteSocketAddress)
        startApplicationConnection()
        while (clientSocket.isConnected && !clientSocket.isClosed) {
            serviceNextClientRequest()
        }
        logger.info("Interactive TCP connection close for {} @ {}", remoteCall, clientSocket.remoteSocketAddress)
    }

}