package pakcatt.network.tcp

import org.slf4j.LoggerFactory
import org.springframework.core.task.TaskExecutor
import org.springframework.stereotype.Component
import pakcatt.application.shared.AppService
import pakcatt.util.StringUtils
import java.net.ServerSocket

@Component
class TCPServerListener(private val appService: AppService,
                        private val myCall: String,
                        private val regexForCallsignValidation: String,
                        private val tcpInteractivePort: Int,
                        private val preWelcomeMessage: String,
                        private val callsignRegexFailMessage: String,
                        private val taskExecutor: TaskExecutor,
                        private val stringUtils: StringUtils): Runnable {

    private val logger = LoggerFactory.getLogger(TCPServerListener::class.java)
    private var tcpServerSocket = ServerSocket(tcpInteractivePort)

    fun handleNewClientConnection() {
        logger.trace("Waiting for new client connection")
        val clientSocket = tcpServerSocket.accept()
        logger.info("Opening interactive TCP connection for client {}", clientSocket.remoteSocketAddress)
        val newClient = TCPClientConnection(clientSocket, appService, myCall, regexForCallsignValidation, preWelcomeMessage, callsignRegexFailMessage, stringUtils)
        taskExecutor.execute(newClient)
    }

    override fun run() {
        logger.info("Listening for interactive TCP sessions on port {}", tcpInteractivePort)
        while (true) {
            handleNewClientConnection()
        }
    }

}