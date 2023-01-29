package pakcatt.network.radio.tnc

import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class TNCDataStreamTCP(
    channelIdentifier: String,
    private val hostAddress: String,
    private val hostPort: Int): TNCDataStream(channelIdentifier) {

    private val logger = LoggerFactory.getLogger(TNCDataStreamTCP::class.java)
    private var tcpSocket = Socket(hostAddress, hostPort)

    override fun connect() {
        logger.info("Connecting to TCP TNC port $hostAddress port $hostPort")
        inputStream = DataInputStream(tcpSocket.inputStream)
        outputStream = DataOutputStream(tcpSocket.outputStream)
    }

    override fun disconnect() {
        logger.info("Disconnecting from TCP TNC port $hostAddress port $hostPort")
        tcpSocket.close()
        inputStream = null
        outputStream = null
    }

    override fun isConnected(): Boolean {
        return tcpSocket.isConnected
    }

}