package pakcatt.network.radio.tnc

import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.Socket

class TNCDataStreamTCP(
    channelIdentifier: String,
    private val hostAddress: String,
    private val hostPort: Int): TNCDataStream(channelIdentifier) {

    private val logger = LoggerFactory.getLogger(TNCDataStreamTCP::class.java)
    private var tcpSocket: Socket? = null

    override fun connect() {
        logger.info("Connecting to TCP TNC: {} host: {} port: {}", channelIdentifier, hostAddress, hostPort)
        try {
            tcpSocket = Socket(hostAddress, hostPort)
            val mySocket = tcpSocket
            if (null != mySocket) {
                inputStream = DataInputStream(mySocket.inputStream)
                outputStream = DataOutputStream(mySocket.outputStream)
            } else {
                logger.error("Could not connect to TCP TNC {}, socket was null", channelIdentifier)
            }
        } catch (e: IOException) {
            logger.error("Could not connect to TCP TNC {} due to error {}", channelIdentifier, e.message)
        }
    }

    override fun disconnect() {
        logger.info("Disconnecting from TCP TNC: {} host: {} port: {}", channelIdentifier, hostAddress, hostPort)
        tcpSocket?.close()
        tcpSocket = null
        inputStream = null
        outputStream = null
    }

    override fun isConnected(): Boolean {
        val mySocket = tcpSocket
        return null != mySocket && mySocket.isConnected && !mySocket.isClosed
    }

}