package pakcatt.network.radio.tnc

import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream

abstract class TNCDataStream(channelIdentifier: String): TNC(channelIdentifier) {

    private val logger = LoggerFactory.getLogger(TNCDataStream::class.java)
    protected var inputStream: DataInputStream? = null
    protected var outputStream: DataOutputStream? = null

    override fun sendData(outputData: ByteArray) {
        logger.trace("TNC sending data of size: ${outputData.size} bytes")
        val myOutputStream = outputStream
        if (null != myOutputStream) {
            myOutputStream.write(outputData)
        } else {
            logger.error("Tried to send a line to the TNC but it's not connected.")
        }
    }

    override fun sendData(outputData: Int) {
        try {
            logger.trace("TNC sending data of size: 1 byte")
            val myOutputStream = outputStream
            if (null != myOutputStream) {
                myOutputStream.write(outputData)
            } else {
                logger.error("Tried to send a line to the TNC port but it's not connected.")
                disconnect()
            }
        } catch (e: Exception) {
            logger.error("Error while trying to send data to a TNC {}", e.message)
            disconnect()
        }
    }

    override fun serviceTNCOutputBuffer() {
        try {
            if (isConnected() && tncHasDataWaiting()) {
                val myInputStream = inputStream
                if (null != myInputStream) {
                    val byteIn = myInputStream.readByte()
                    receiveDataCallback(byteIn)
                } else {
                    logger.error("Error while trying to service incoming TNC buffer, input stream was null")
                    disconnect()
                }
            }
        } catch (e: Exception) {
            logger.error("Error while trying to service incoming TNC buffer {}", e.message)
            disconnect()
        }
    }

    private fun tncHasDataWaiting(): Boolean {
        val myInputStream = inputStream
        return null != myInputStream && myInputStream.available() > 0
    }

}