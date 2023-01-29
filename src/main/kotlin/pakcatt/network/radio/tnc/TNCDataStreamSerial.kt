package pakcatt.network.radio.tnc

import gnu.io.NRSerialPort
import org.slf4j.LoggerFactory
import java.io.DataInputStream
import java.io.DataOutputStream

class TNCDataStreamSerial(
    channelIdentifier: String,
    private val serialPortPath: String,
    private val serialPortBaud: Int): TNCDataStream(channelIdentifier) {

    private val logger = LoggerFactory.getLogger(TNCDataStreamSerial::class.java)
    private val serial = NRSerialPort(serialPortPath, serialPortBaud)

    override fun connect() {
        logger.info("Connecting to serial port $serialPortPath at $serialPortBaud baud")
        serial.connect()
        inputStream = DataInputStream(serial.inputStream)
        outputStream = DataOutputStream(serial.outputStream)
    }

    override fun disconnect() {
        logger.info("Disconnecting from serial port $serialPortPath")
        serial.disconnect()
        inputStream = null
        outputStream = null
    }

    override fun isConnected(): Boolean {
        return serial.isConnected
    }

}