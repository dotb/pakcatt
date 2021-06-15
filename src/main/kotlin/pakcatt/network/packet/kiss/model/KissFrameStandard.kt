package pakcatt.network.packet.kiss.model

open class KissFrameStandard: KissFrame() {

    private var controlField: Byte = byteUtils.intToByte(0x00)

    override fun populateFromFrameData(frameByteData: ByteArray) {
        super.populateFromFrameData(frameByteData)
        if (frameByteData.size >= 16) {
            controlField = frameByteData[15]
        }
    }

    override fun controlBits(): ByteArray {
        return byteArrayOf(controlField)
    }

    override fun pollFinalBit(): Boolean {
        return byteUtils.compareMaskedByte(controlField,0x10, 0x10)
    }

    override fun calculateControlFrame(): ControlField {
        for (controlType in ControlField.values()) {
            if (byteUtils.compareMaskedByte(controlField, controlType.mask, controlType.bitPattern)) {
                return controlType
            }
        }
        return ControlField.UNKNOWN_Field
    }

    override fun setControlFrame(controlType: ControlField, receiveSeq: Int, sendSeq: Int) {
        /* Ensure the controlField starts at 0 so that residual bits don't remain and break
         * the control type, send or receive sequence number bits. */
        controlField = byteUtils.intToByte(0x00)
        // Set the control type bits first
        controlField = byteUtils.setBits(controlField, controlType.bitPattern)
        // Then set the send and receive sequence bits. No change is made if they are 0.
        setSendSequenceNumberBits(sendSeq)
        setReceiveSequenceNumberBits(receiveSeq)
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

    private fun setReceiveSequenceNumberBits(receiveSeq: Int) {
        val sequenceNumberByte = byteUtils.intToByte(receiveSeq)
        val shiftedSequence = byteUtils.shiftBitsLeft(sequenceNumberByte, 5)
        this.controlField = byteUtils.setBits(controlField, shiftedSequence)
    }

    private fun setSendSequenceNumberBits(sendSeq: Int) {
        val sequenceNumberByte = byteUtils.intToByte(sendSeq)
        val shiftedSequence = byteUtils.shiftBitsLeft(sequenceNumberByte, 1)
        this.controlField = byteUtils.setBits(controlField, shiftedSequence)
    }

}