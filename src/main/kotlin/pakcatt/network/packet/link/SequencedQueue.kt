package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import pakcatt.network.packet.kiss.ControlFrame
import pakcatt.network.packet.kiss.KissFrame
import java.util.*
import kotlin.math.min

class SequencedQueue {

    private val logger = LoggerFactory.getLogger(SequencedQueue::class.java)
    private val maxSequenceNumberSize = 8
    private val maxDeliveryAttempts = 10
    private val deliveryRetryTimeMilliseconds = 6000
    private var sequencedFramesForDelivery = ArrayList<KissFrame>(maxSequenceNumberSize)

    /* Section 4.2.4 Frame Variables and Sequence Numbers, Beech et all */
    /* The send state variable contains the next sequential number to be assigned to the next transmitted I frame.
     * This variable is updated with the transmission of each I frame.*/
    private var ourNextUnboundedSendSequenceNumber = 0

    /* The acknowledge state variable contains the sequence number of the last
     * frame acknowledged by its peer [V(A)-1 equals the N(S) of the last acknowledged I frame]. */
    private var nextSendSequenceNumberExpectedByPeer = 0

    fun reset() {
        sequencedFramesForDelivery = ArrayList<KissFrame>()
        ourNextUnboundedSendSequenceNumber = 0
        nextSendSequenceNumberExpectedByPeer = 0
    }

    fun addDataFrameForSequencedTransmission(newFrame: KissFrame) {
        newFrame.setSendSequenceNumber(ourNextBoundedSendSequenceNumber())
        sequencedFramesForDelivery.add(newFrame)
        ourNextUnboundedSendSequenceNumber++
        val lowerAcknowledgeNumberLimit = ourNextUnboundedSendSequenceNumber - maxSequenceNumberSize + 1
        val lowerBoundedAcknowledgedNumber = convertUnboundedIndexToSequenceNumber(lowerAcknowledgeNumberLimit)
        if (lowerBoundedAcknowledgedNumber + maxSequenceNumberSize <= ourNextUnboundedSendSequenceNumber) {
            logger.error("The sequenced send queue index {} has advanced too far ahead of the acknowledged frame index {}", ourNextUnboundedSendSequenceNumber, lowerBoundedAcknowledgedNumber)
        }
    }

    fun getSequencedFramesForDelivery(): LinkedList<KissFrame> {
        val timeStampNow = Date().time
        val startIndex = convertBoundedSequenceNumberToIndex(nextSendSequenceNumberExpectedByPeer)
        val endIndex = min((startIndex + 2), ourNextUnboundedSendSequenceNumber - 1)

        var framesForDelivery = LinkedList<KissFrame>()
        for (index in startIndex..endIndex) {
            val frameAwaitingDelivery = sequencedFramesForDelivery[index]
            if (frameAwaitingDelivery.deliveryAttempts < maxDeliveryAttempts
                && frameAwaitingDelivery.lastDeliveryAttemptTimeStamp < timeStampNow - deliveryRetryTimeMilliseconds) {
                // Attempt to deliver this frame
                frameAwaitingDelivery.deliveryAttempts++
                frameAwaitingDelivery.lastDeliveryAttemptTimeStamp = timeStampNow
                framesForDelivery.add(frameAwaitingDelivery)
            }
        }
        return framesForDelivery
    }

    // Keep a record of the next send sequence number our remote party expects us to send
    fun handleIncomingAcknowledgementAndIfRepeated(incomingFrame: KissFrame): Boolean {
        return if (nextSendSequenceNumberExpectedByPeer == incomingFrame.receiveSequenceNumber()) {
            true
        } else {
            nextSendSequenceNumberExpectedByPeer = incomingFrame.receiveSequenceNumber()
            false
        }
    }

    /**
     * Return the next bounded (0 - 7) send sequence number
     */
    private fun ourNextBoundedSendSequenceNumber(): Int {
        return convertUnboundedIndexToSequenceNumber(ourNextUnboundedSendSequenceNumber)
    }

    /**
     * Convert an unbounded positive integer to a value between
     * 0 and 7.
     */
    private fun convertUnboundedIndexToSequenceNumber(index: Int): Int {
        return if (ourNextUnboundedSendSequenceNumber < maxSequenceNumberSize) {
            index
        } else {
            val multiplier = index / maxSequenceNumberSize
            val valueToSubtract = multiplier * maxSequenceNumberSize
            index - valueToSubtract
        }
    }

    /**
     * Convert a bounded send sequence number to an unbounded index
     * that maps to a frame in the sequenced frame array that is at a
     * position lower than ourNextUnboundedSendSequenceNumber.
     */
    private fun convertBoundedSequenceNumberToIndex(boundedSequenceNumber: Int): Int {
        // We have to assume the bounded number is within the 7 positions <= ourNextUnboundedSendSequenceNumber
        return if (boundedSequenceNumber <= ourNextBoundedSendSequenceNumber()) {
            val difference = ourNextBoundedSendSequenceNumber() - boundedSequenceNumber
            ourNextUnboundedSendSequenceNumber - difference
        } else {
            val difference = boundedSequenceNumber - ourNextBoundedSendSequenceNumber()
            ourNextUnboundedSendSequenceNumber + difference - maxSequenceNumberSize
        }
    }

}