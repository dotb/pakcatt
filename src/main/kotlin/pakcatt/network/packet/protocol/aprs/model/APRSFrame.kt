package pakcatt.network.packet.protocol.aprs.model

import pakcatt.network.packet.kiss.model.KissFrame
import pakcatt.network.packet.kiss.model.KissFrameStandard

enum class APRSDataType(val id: Int) {
    MIC_E(0x1C), MIC_E_OLD(0x1D), MIC_E_C(0x60), LOC_NO_TIME(0x21),
    ITEM(0x29), LOC_WITH_TIME(0x21), MESSAGE(0x3A),
    OBJECT(0x3B), LOC_NO_TIME_WITH_MESSAGE(0x3D), STATUS(0x3E),
    LOC_WITH_TIME_AND_MESSAGE(0x40), TELEMETRY(0x54), WEATHER(0x5F),
    UNKNOWN(0x00)
}

open class APRSFrame: KissFrameStandard() {

    private var aprsDataType: APRSDataType = APRSDataType.UNKNOWN

    open fun populateFromKissFrame(kissFrame: KissFrame) {
        populateFromFrameData(kissFrame.packetData())
    }

    override fun populateFromFrameData(frameByteData: ByteArray) {
        super.populateFromFrameData(frameByteData)
        if (payloadData.isNotEmpty()) {
            setDataType(payloadData[0])
        }
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("Data Type: $aprsDataType ")
        stringBuilder.append("From: ${sourceCallsign()} ")
        if (payloadData.isNotEmpty()) {
            stringBuilder.append("Payload: ${payloadDataString()}")
        }

        return stringBuilder.toString()
    }

    fun aprsDataType(): APRSDataType {
        return aprsDataType
    }

    private fun setDataType(dataTypeByte: Byte) {
        this.aprsDataType = when (dataTypeByte) {
            byteUtils.intToByte(APRSDataType.MIC_E.id) -> APRSDataType.MIC_E
            byteUtils.intToByte(APRSDataType.MIC_E_OLD.id) -> APRSDataType.MIC_E_OLD
            byteUtils.intToByte(APRSDataType.MIC_E_C.id) -> APRSDataType.MIC_E_C
            byteUtils.intToByte(APRSDataType.LOC_NO_TIME.id) -> APRSDataType.LOC_NO_TIME
            byteUtils.intToByte(APRSDataType.ITEM.id) -> APRSDataType.ITEM
            byteUtils.intToByte(APRSDataType.LOC_WITH_TIME.id) -> APRSDataType.LOC_WITH_TIME
            byteUtils.intToByte(APRSDataType.MESSAGE.id) -> APRSDataType.MESSAGE
            byteUtils.intToByte(APRSDataType.OBJECT.id) -> APRSDataType.OBJECT
            byteUtils.intToByte(APRSDataType.LOC_NO_TIME_WITH_MESSAGE.id) -> APRSDataType.LOC_NO_TIME_WITH_MESSAGE
            byteUtils.intToByte(APRSDataType.STATUS.id) -> APRSDataType.STATUS
            byteUtils.intToByte(APRSDataType.LOC_WITH_TIME_AND_MESSAGE.id) -> APRSDataType.LOC_WITH_TIME_AND_MESSAGE
            byteUtils.intToByte(APRSDataType.TELEMETRY.id) -> APRSDataType.TELEMETRY
            byteUtils.intToByte(APRSDataType.WEATHER.id) -> APRSDataType.WEATHER
            else -> APRSDataType.UNKNOWN
        }
    }

}