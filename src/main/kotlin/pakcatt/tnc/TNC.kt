package pakcatt.tnc

abstract class TNC {

    internal lateinit var receiveDataCallback:(receivedData: Byte) -> Unit?

    fun setReceiveDataCallback(newCallback: (receivedData: Byte) -> Unit) {
        receiveDataCallback = newCallback
    }

    abstract fun connect()

    abstract fun disconnect()

    abstract fun isConnected(): Boolean

    abstract fun sendData(outputData: ByteArray)

    abstract fun sendData(outputData: Int)

}