package pakcatt.network.radio.tnc

import gnu.io.NRSerialPort
import gnu.io.NoSuchPortException
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.DataInputStream
import java.io.DataOutputStream

@Component
@Profile("production")
class TNCSerial(private val serialPortPath: String,
                private val serialPortBaud: Int): TNC() {

    private val logger = LoggerFactory.getLogger(TNCSerial::class.java)
    private val serial = NRSerialPort(serialPortPath, serialPortBaud)
    private var inputStream: DataInputStream? = null
    private var outputStream: DataOutputStream? = null
    private var shouldBeConnected: Boolean = false

    override fun connect() {
        shouldBeConnected = true
    }

    override fun disconnect() {
        logger.info("Disconnecting from serial port $serialPortPath")
        serial.disconnect()
        inputStream = null
        outputStream = null
        shouldBeConnected = false
    }

    override fun isConnected(): Boolean {
        return serial.isConnected
    }

    override fun sendData(outputData: ByteArray) {
        logger.trace("Serial sending data of size: ${outputData.size} bytes")
        val myOutputStream = outputStream
        if (null != myOutputStream) {
            myOutputStream.write(outputData)
        } else {
            logger.error("Tried to send a line to the serial port but it's not connected.")
        }
    }

    override fun sendData(outputData: Int) {
        logger.trace("Serial sending data of size: 1 byte")
        val myOutputStream = outputStream
        if (null != myOutputStream) {
            myOutputStream.write(outputData)
        } else {
            logger.error("Tried to send a line to the serial port but it's not connected.")
        }
    }

    @Scheduled(fixedRate = 1)
    private fun serviceSerialPort() {
        try {
            if (isConnected() && serialHasInputWaiting()) {
                val myInputStream = inputStream
                if (null != myInputStream) {
                    val byteIn = myInputStream.readByte()
                    receiveDataCallback(byteIn)
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    /**
     * Attempt to connect to the serial TNC, with a 10 second retry.
     */
    @Scheduled(fixedRate = 10000)
    private fun connectToTNC() {
        try {
            if (shouldBeConnected && !isConnected()) {
                logger.info("Connecting to serial port $serialPortPath at $serialPortBaud baud")
                serial.connect()
                inputStream = DataInputStream(serial.inputStream)
                outputStream = DataOutputStream(serial.outputStream)
            }
        } catch (e: Exception) {
            logger.error("Could not connect to the serial TNC", e)
        }
    }

    private fun serialHasInputWaiting(): Boolean {
        val myInputStream = inputStream
        return null != myInputStream && myInputStream.available() > 0
    }

}