package pakcatt.network.packet.kiss

import pakcatt.util.ByteUtils

open class KissFrameExtended: KissFrame() {

    private var controlFieldLow: Byte = byteUtils.intToByte(0x00)
    private var controlFieldHigh: Byte = byteUtils.intToByte(0x00)

    fun parseRawKISSFrame(portAndCommand: Byte,
                          destCallsign: ByteArray,
                          destSSID: Byte,
                          sourceCallsign: ByteArray,
                          sourceSSID: Byte,
                          controlFieldLow: Byte,
                          controlFieldHigh: Byte,
                          protocolID: Byte,
                          payloadData: ByteArray) {
        super.parseRawKISSFrame(portAndCommand,
            destCallsign,
            destSSID,
            sourceCallsign,
            sourceSSID,
            protocolID,
            payloadData)

        this.controlFieldLow = controlFieldLow
        this.controlFieldHigh = controlFieldHigh
    }

    override fun controlField(): ByteArray {
        return byteArrayOf(controlFieldHigh, controlFieldLow)
    }

    override fun pollFinalBit(): Boolean {
        return byteUtils.compareMaskedByte(controlFieldHigh,0x01, 0x01)
    }

    override fun calculateControlFrame(): ControlFrame {
        for (controlType in ControlFrame.values()) {
            val controlTypeLowInt = byteUtils.maskInt(controlType.bitPattern, 0x000000FF)
            val maskLow = byteUtils.maskInt(controlType.mask, 0x000000FF)
            val controlTypeHighInt = byteUtils.shiftBitsRight(controlType.bitPattern, 8)
            val maskHigh = byteUtils.shiftBitsRight(controlType.mask, 8)
            if (byteUtils.compareMaskedByte(controlFieldLow, maskLow, controlTypeLowInt) && byteUtils.compareMaskedByte(controlFieldLow, maskHigh, controlTypeHighInt)) {
                return controlType
            }
        }
        return ControlFrame.UNKNOWN_FRAME
    }

    override fun setControlFrame(controlType: ControlFrame) {
        val controlTypeLowInt = byteUtils.maskInt(controlType.bitPattern, 0x000000FF)
        val controlTypeHighInt = byteUtils.shiftBitsRight(controlType.bitPattern, 8)
        controlFieldLow = byteUtils.setBits(controlFieldLow, controlTypeLowInt)
        controlFieldHigh = byteUtils.setBits(controlFieldHigh, controlTypeHighInt)
    }

    override fun calculateReceiveSequenceNumber(): Int {
        val shiftedControlField = byteUtils.shiftBitsRight(controlFieldHigh, 1)
        return byteUtils.byteToInt(shiftedControlField)
    }

    override fun calculateSendSequenceNumber(): Int {
        val shiftedControlField = byteUtils.shiftBitsRight(controlFieldLow, 1)
        return byteUtils.byteToInt(shiftedControlField)
    }

    override fun setReceiveSequenceNumber(receiveSeq: Int) {
        val sequenceNumberByte = byteUtils.intToByte(receiveSeq)
        val shiftedSequence = byteUtils.shiftBitsLeft(sequenceNumberByte, 1)
        this.controlFieldHigh = byteUtils.setBits(controlFieldHigh, shiftedSequence)
    }

    override fun setSendSequenceNumber(sendSeq: Int) {
        val sequenceNumberByte = byteUtils.intToByte(sendSeq)
        val shiftedSequence = byteUtils.shiftBitsLeft(sequenceNumberByte, 1)
        this.controlFieldLow = byteUtils.setBits(controlFieldLow, shiftedSequence)
    }

}