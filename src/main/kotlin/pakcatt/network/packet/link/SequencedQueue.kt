package pakcatt.network.packet.link

import org.slf4j.LoggerFactory
import pakcatt.network.packet.kiss.KissFrame
import java.util.*
import kotlin.math.min

class SequencedQueue {

    private val logger = LoggerFactory.getLogger(SequencedQueue::class.java)
    private val dataFramesSentPerOver = 2
    private val maxSequenceNumberSize = 8
    private val maxDeliveryAttempts = 10
    private val deliveryRetryTimeMilliseconds = 5000
    private var sequencedFramesForDelivery = ArrayList<KissFrame>(maxSequenceNumberSize)

    /* Section 4.2.4 Frame Variables and Sequence Numbers, Beech et all */
    /* The send state variable contains the next sequential number to be assigned to the next transmitted I frame.
     * This variable is updated with the transmission of each I frame.*/
    private var ourNextUnboundedSendSequenceNumber = 0

    /* The nextBoundedSendNumberExpectedByPeer state variable contains the sequence number of the last
     * frame acknowledged by our remote peer [V(A)-1 equals the N(S) of the last acknowledged I frame]. */
    private var nextBoundedSendNumberExpectedByPeer = 0
    private var nextUnboundedFrameIndexExpectedByPeer = 0

    fun reset() {
        sequencedFramesForDelivery = ArrayList<KissFrame>()
        ourNextUnboundedSendSequenceNumber = 0
        nextBoundedSendNumberExpectedByPeer = 0
        nextUnboundedFrameIndexExpectedByPeer = 0
    }

    fun addFrameForSequencedTransmission(newFrame: KissFrame) {
        if (newFrame.requiresAcknowledgement()) {
            newFrame.setSendSequenceNumber(ourNextBoundedSendSequenceNumber())
            ourNextUnboundedSendSequenceNumber++
        }
        sequencedFramesForDelivery.add(newFrame)
    }

    fun getSequencedFramesForDelivery(): LinkedList<KissFrame> {
        val timeStampNow = Date().time
        val startIndex = nextUnboundedFrameIndexExpectedByPeer
        val endIndex = min((startIndex + dataFramesSentPerOver - 1), ourNextUnboundedSendSequenceNumber - 1)
        var framesForDelivery = LinkedList<KissFrame>()

        if (startIndex >= 0 && endIndex >= 0) {
            for (index in startIndex..endIndex) {
                val frameAwaitingDelivery = sequencedFramesForDelivery[index]
                if (frameAwaitingDelivery.deliveryAttempts < maxDeliveryAttempts
                    && frameAwaitingDelivery.lastDeliveryAttemptTimeStamp < timeStampNow - deliveryRetryTimeMilliseconds
                ) {
                    // Attempt to deliver this frame
                    frameAwaitingDelivery.deliveryAttempts++
                    frameAwaitingDelivery.lastDeliveryAttemptTimeStamp = timeStampNow
                    framesForDelivery.add(frameAwaitingDelivery)
                }
            }
        }
        return framesForDelivery
    }

    /**
     * Returns true if the current or next call to getSequencedFramesForDelivery
     * returns the last frames remaining in the delivery queue.
     */
    fun isAtEndOfMessageDelivery(): Boolean {
        return nextUnboundedFrameIndexExpectedByPeer >= ourNextUnboundedSendSequenceNumber - dataFramesSentPerOver
    }

    // Keep a record of the next send sequence number our remote party expects us to send
    fun handleIncomingAcknowledgementAndIfRepeated(incomingFrame: KissFrame): Boolean {
        return if (nextBoundedSendNumberExpectedByPeer == incomingFrame.receiveSequenceNumber()) {
            true
        } else {
            /*
             * Calculate the difference between this value and the previous received value and
             * use it to adjust the unbounded index that points to the next frame expected
             * to be received by our remote peer.
             */
            val difference = when {
                incomingFrame.receiveSequenceNumber() > nextBoundedSendNumberExpectedByPeer -> incomingFrame.receiveSequenceNumber() - nextBoundedSendNumberExpectedByPeer
                else -> incomingFrame.receiveSequenceNumber() + maxSequenceNumberSize - nextBoundedSendNumberExpectedByPeer
            }
            nextUnboundedFrameIndexExpectedByPeer += difference
            nextBoundedSendNumberExpectedByPeer = incomingFrame.receiveSequenceNumber()
            false
        }
    }

    /**
     * Return the next bounded (0 - 7) send sequence number.
     * Converts an unbounded positive integer to a value between
     * 0 and 7.
     */
    private fun ourNextBoundedSendSequenceNumber(): Int {
        return ourNextUnboundedSendSequenceNumber % 8
    }

}