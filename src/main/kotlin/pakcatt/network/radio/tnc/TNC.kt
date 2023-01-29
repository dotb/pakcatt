package pakcatt.network.radio.tnc

abstract class TNC(val channelIdentifier: String) {

    internal lateinit var receiveDataCallback:(receivedData: Byte) -> Unit?

    fun setReceiveDataCallback(newCallback: (receivedData: Byte) -> Unit) {
        receiveDataCallback = newCallback
    }

    abstract fun connect()

    abstract fun disconnect()

    abstract fun isConnected(): Boolean

    abstract fun sendData(outputData: ByteArray)

    abstract fun sendData(outputData: Int)

    abstract fun serviceTNCOutputBuffer()

}