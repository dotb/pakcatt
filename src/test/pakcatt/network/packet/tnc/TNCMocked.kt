package pakcatt.network.packet.tnc

import org.springframework.stereotype.Component
import pakcatt.util.ByteUtils

@Component
class TNCMocked: TNC() {

    private val maxBufferSize = 8192
    private val byteUtils = ByteUtils()
    private var isConnected = false
    private var dataBuffer = ByteArray(maxBufferSize)
    private var dataIndex = 0

    fun clearDataBuffer() {
        dataBuffer = ByteArray(maxBufferSize)
        dataIndex = 0
    }

    fun dataBuffer(): ByteArray {
        return dataBuffer.copyOf(dataIndex)
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
        dataBuffer[dataIndex] = byteUtils.intToByte(outputData)
        dataIndex++
    }

}