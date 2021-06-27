package pakcatt.network.radio.protocol.aprs.model

import pakcatt.network.radio.kiss.model.ControlField
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.network.radio.kiss.model.KissFrameStandard

enum class APRSDataType(val id: Int) {
    // MIC-E
    MIC_E(0x1C), // FS
    MIC_E_OLD(0x1D), // GS
    MIC_E_DATA(0x60), // `
    MIC_E_DATA_OLD(0x27), // '

    // Location
    LOC_NO_TIME_NO_MESSAGE(0x21), // !
    LOC_NO_TIME_WITH_MESSAGE(0x3D), // =
    LOC_WITH_TIME_NO_MESSAGE(0x2F), // /
    LOC_WITH_TIME_WITH_MESSAGE(0x40), // @
    SHELTER_WITH_TIME(0x2B), // +
    RAW_GPS(0x24), // $
    MAP_FEATURE(0x26), // &

    // Weather
    WEATHER_SPACE(0x2E), // .
    WEATHER_NO_LOC(0x5F), // _
    WEATHER_PEET_BROS_1(0x23), // #
    WEATHER_PEET_BROS_2(0x2A), // *

    // Message
    MESSAGE(0x3A), // :

    // Other
    AGRELO_DFJR(0x25), // %
    ITEM(0x29), // )
    OBJECT(0x3B), // ;
    STATION_CAPABILITIES(0x2C), // <
    STATUS(0x3E), // >
    QUERY(0x3F), // ?
    TELEMETRY(0x54), // T
    MAIDENHEAD_BEACON(0x5B), // [
    USER_DEFINED_APRS(0x7B), // {
    THIRD_PARTY_TRAFFIC(0x7D), // }
    TEST(0x2C), // ,
    UNKNOWN(0x00)
}

open class APRSFrame: KissFrameStandard() {

    protected var aprsDataType: APRSDataType = APRSDataType.UNKNOWN

    init {
        // Configure the AX.25 frame parameters for APRS
        setControlField(ControlField.U_UNNUMBERED_INFORMATION)
        // Version identifier goes into teh destination callsign
        setDestCallsign("PAKCAT-0")
    }


    open fun populateFromKissFrame(kissFrame: KissFrame): APRSFrame {
        return populateFromFrameData(kissFrame.packetData())
    }

    override fun populateFromFrameData(frameByteData: ByteArray): APRSFrame {
        super.populateFromFrameData(frameByteData)
        if (payloadData.isNotEmpty()) {
            setDataType(payloadData[0])
        }
        return this
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

    private fun setDataType(requestedAPRSDataType: Byte) {
        this.aprsDataType = APRSDataType.UNKNOWN // Start with an unknown type
        for (aprsDataTypeOption in APRSDataType.values()) {
            val dataTypeIntValue = byteUtils.byteToInt(requestedAPRSDataType)
            if (aprsDataTypeOption.id == dataTypeIntValue) {
                this.aprsDataType = aprsDataTypeOption
            }
        }
    }

}