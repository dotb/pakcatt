package pakcatt.network.packet.kiss

class KissFrameStandard: KissFrame() {

    private var controlField: Byte = byteUtils.intToByte(0x00)

    fun parseRawKISSFrame(portAndCommand: Byte,
                          destCallsign: ByteArray,
                          destSSID: Byte,
                          sourceCallsign: ByteArray,
                          sourceSSID: Byte,
                          controlField: Byte,
                          protocolID: Byte,
                          payloadData: ByteArray) {
        super.parseRawKISSFrame(portAndCommand,
                                destCallsign,
                                destSSID,
                                sourceCallsign,
                                sourceSSID,
                                protocolID,
                                payloadData)
        this.controlField = controlField
    }

    override fun controlField(): ByteArray {
        return byteArrayOf(controlField)
    }

    override fun pollFinalBit(): Boolean {
        return byteUtils.compareMaskedByte(controlField,0x10, 0x10)
    }

    override fun calculateControlFrame(): ControlFrame {
        for (controlType in ControlFrame.values()) {
            if (byteUtils.compareMaskedByte(controlField, controlType.mask, controlType.bitPattern)) {
                return controlType
            }
        }
        return ControlFrame.UNKNOWN_FRAME
    }

    override fun setControlFrame(controlType: ControlFrame) {
        controlField = byteUtils.setBits(controlField, controlType.bitPattern)
    }

    override fun calculateReceiveSequenceNumber(): Int {
        val shiftedControlField = byteUtils.shiftBitsRight(controlField, 5)
        return byteUtils.byteToInt(shiftedControlField)
    }

    override fun calculateSendSequenceNumber(): Int {
        val shiftedControlField = byteUtils.shiftBitsRight(controlField, 1)
        val maskedControlField = byteUtils.maskByte(shiftedControlField, 0x07)
        return byteUtils.byteToInt(maskedControlField)
    }

    override fun setReceiveSequenceNumber(receiveSeq: Int) {
        val sequenceNumberByte = byteUtils.intToByte(receiveSeq)
        val shiftedSequence = byteUtils.shiftBitsLeft(sequenceNumberByte, 5)
        this.controlField = byteUtils.setBits(controlField, shiftedSequence)
    }

    override fun setSendSequenceNumber(sendSeq: Int) {
        val sequenceNumberByte = byteUtils.intToByte(sendSeq)
        val shiftedSequence = byteUtils.shiftBitsLeft(sequenceNumberByte, 1)
        this.controlField = byteUtils.setBits(controlField, shiftedSequence)
    }

}