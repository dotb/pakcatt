package pakcatt.network.radio.kiss.model

open class KissFrameExtended: KissFrame() {

    private var controlFieldLow: Byte = byteUtils.intToByte(0x00)
    private var controlFieldHigh: Byte = byteUtils.intToByte(0x00)

    override fun setControlFieldFromFrameData(frameByteData: ByteArray, nextIndex: Int): Int {
        controlFieldHigh = frameByteData[nextIndex]
        controlFieldLow = frameByteData[nextIndex + 1]
        return nextIndex + 2
    }

    override fun controlBits(): ByteArray {
        return byteArrayOf(controlFieldHigh, controlFieldLow)
    }

    override fun pollFinalBit(): Boolean {
        return byteUtils.compareMaskedByte(controlFieldHigh,0x01, 0x01)
    }

    override fun calculateControlFrame(): ControlField {
        for (controlType in ControlField.values()) {
            val controlTypeLowInt = byteUtils.maskInt(controlType.bitPattern, 0x000000FF)
            val maskLow = byteUtils.maskInt(controlType.mask, 0x000000FF)
            val controlTypeHighInt = byteUtils.shiftBitsRight(controlType.bitPattern, 8)
            val maskHigh = byteUtils.shiftBitsRight(controlType.mask, 8)
            if (byteUtils.compareMaskedByte(controlFieldLow, maskLow, controlTypeLowInt) && byteUtils.compareMaskedByte(controlFieldLow, maskHigh, controlTypeHighInt)) {
                return controlType
            }
        }
        return ControlField.UNKNOWN_Field
    }

    override fun setControlFrame(controlType: ControlField, receiveSeq: Int, sendSeq: Int) {
        // Ensure the controlField starts at 0 so that residual bits don't remain
        controlFieldHigh = byteUtils.intToByte(0x00)
        controlFieldLow = byteUtils.intToByte(0x00)
        // Set the control type
        val controlTypeLowInt = byteUtils.maskInt(controlType.bitPattern, 0x000000FF)
        val controlTypeHighInt = byteUtils.shiftBitsRight(controlType.bitPattern, 8)
        controlFieldLow = byteUtils.setBits(controlFieldLow, controlTypeLowInt)
        controlFieldHigh = byteUtils.setBits(controlFieldHigh, controlTypeHighInt)
        // Then set the send and receive sequence bits. No change is made if they are 0.
        setSendSequenceNumber(sendSeq)
        setReceiveSequenceNumberBits(receiveSeq)
    }

    override fun calculateReceiveSequenceNumber(): Int {
        val shiftedControlField = byteUtils.shiftBitsRight(controlFieldHigh, 1)
        return byteUtils.byteToInt(shiftedControlField)
    }

    override fun calculateSendSequenceNumber(): Int {
        val shiftedControlField = byteUtils.shiftBitsRight(controlFieldLow, 1)
        return byteUtils.byteToInt(shiftedControlField)
    }

    private fun setReceiveSequenceNumberBits(receiveSeq: Int) {
        val sequenceNumberByte = byteUtils.intToByte(receiveSeq)
        val shiftedSequence = byteUtils.shiftBitsLeft(sequenceNumberByte, 1)
        this.controlFieldHigh = byteUtils.setBits(controlFieldHigh, shiftedSequence)
    }

    private fun setSendSequenceNumberBits(sendSeq: Int) {
        val sequenceNumberByte = byteUtils.intToByte(sendSeq)
        val shiftedSequence = byteUtils.shiftBitsLeft(sequenceNumberByte, 1)
        this.controlFieldLow = byteUtils.setBits(controlFieldLow, shiftedSequence)
    }

}