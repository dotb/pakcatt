package pakcatt.network.radio.tnc

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.util.ByteUtils

@Component
@Profile("test")
class TNCMocked: TNC("Mocked TNC") {

    private val maxBufferSize = 8192
    private val byteUtils = ByteUtils()
    private var isConnected = false
    private var sentDataBuffer = ByteArray(maxBufferSize)
    private var dataIndex = 0

    fun clearDataBuffer() {
        sentDataBuffer = ByteArray(maxBufferSize)
        dataIndex = 0
    }

    fun sentDataBuffer(): ByteArray {
        return sentDataBuffer.copyOf(dataIndex)
    }

    override fun connect() {
        isConnected = true
    }

    override fun disconnect() {
        isConnected = false
    }

    override fun isConnected(): Boolean {
        return isConnected
    }

    override fun sendData(outputData: ByteArray) {
        for (byte in outputData) {
            val intValue = byteUtils.byteToInt(byte)
            sendData(intValue)
        }
    }

    override fun sendData(outputData: Int) {
        sentDataBuffer[dataIndex] = byteUtils.intToByte(outputData)
        dataIndex++
    }

    override fun serviceTNCOutputBuffer() {
        // This mock TNC does not yet simulate received data that is passed up to the application stack.
    }

}