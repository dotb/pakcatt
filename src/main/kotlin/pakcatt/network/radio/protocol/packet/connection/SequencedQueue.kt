package pakcatt.network.radio.protocol.packet.connection

import org.slf4j.LoggerFactory
import pakcatt.network.radio.kiss.model.KissFrame
import pakcatt.util.SimpleTimer
import java.util.*
import kotlin.math.min

class SequencedQueue(private val framesPerOver: Int,
                     private val maxDeliveryAttempts: Int,
                     private val deliveryRetryTimeSeconds: Int) {

    private val logger = LoggerFactory.getLogger(SequencedQueue::class.java)
    private val maxSequenceNumberSize = 8
    private var sequencedFramesForDelivery = ArrayList<KissFrame>(maxSequenceNumberSize)
    private var deliveryAttemptsRetryTimer = SimpleTimer(deliveryRetryTimeSeconds)

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
        deliveryAttemptsRetryTimer.expireTimer()
    }

    fun remoteStationIsReadyExpireDeliveryTimer() {
        deliveryAttemptsRetryTimer.expireTimer()
    }

    fun addFrameForSequencedTransmission(newFrame: KissFrame) {
        if (newFrame.requiresSendSequenceNumber()) {
            newFrame.setSendSequenceNumber(ourNextBoundedSendSequenceNumber())
            ourNextUnboundedSendSequenceNumber++
        }
        sequencedFramesForDelivery.add(newFrame)
    }

    fun getSequencedFramesForDelivery(): LinkedList<KissFrame> {
        val startIndex = nextUnboundedFrameIndexExpectedByPeer
        val endIndex = min((startIndex + framesPerOver - 1), ourNextUnboundedSendSequenceNumber - 1)
        var framesForDelivery = LinkedList<KissFrame>()

        var retryAttemptsExhausted = false
        if (startIndex >= 0 && endIndex >= 0 && deliveryAttemptsRetryTimer.hasExpired()) {
            for (index in startIndex..endIndex) {
                val frameAwaitingDelivery = sequencedFramesForDelivery[index]
                if (frameAwaitingDelivery.deliveryAttempts < maxDeliveryAttempts) {
                    frameAwaitingDelivery.deliveryAttempts++
                    framesForDelivery.add(frameAwaitingDelivery)
                } else {
                    retryAttemptsExhausted = true
                }
            }
            if (retryAttemptsExhausted) {
                // We've run out of delivery attempts and we need to give up.
                // Rage quit and clear out our send buffer
                logger.debug("Given up on delivering frames in queue. Resetting queue.")
                reset()
            }
            deliveryAttemptsRetryTimer.reset()
        }
        return framesForDelivery
    }

    /**
     * Returns true if the current or next call to getSequencedFramesForDelivery
     * returns the last frames remaining in the delivery queue.
     */
    fun isAtEndOfMessageDelivery(): Boolean {
        return nextUnboundedFrameIndexExpectedByPeer >= ourNextUnboundedSendSequenceNumber - framesPerOver
    }

    // Keep a record of the next send sequence number our remote party expects us to send
    fun updateSequenceNumbersAndCheckIsDuplicate(incomingFrame: KissFrame): Boolean {
        logger.trace("Updating sequence numbers for frame: {}", incomingFrame.toString())
        return if (nextBoundedSendNumberExpectedByPeer == incomingFrame.receiveSequenceNumber()) {
            logger.trace("nextBoundedSendNumberExpectedByPeer is the same as incomingFrame.receiveSequenceNumber() for Frame: {}", incomingFrame.toString())
            true
        } else {
            /*
             * Calculate the difference between this received value and the previous received value, and
             * use it to adjust the unbounded index that points to the next frame expected
             * to be received by our remote peer.
             */
            logger.trace("Calculating nextUnboundedFrameIndexExpectedByPeer based on Frame: {}", incomingFrame.toString())
            val difference = when {
                incomingFrame.receiveSequenceNumber() > nextBoundedSendNumberExpectedByPeer -> incomingFrame.receiveSequenceNumber() - nextBoundedSendNumberExpectedByPeer
                else -> incomingFrame.receiveSequenceNumber() + maxSequenceNumberSize - nextBoundedSendNumberExpectedByPeer
            }
            val updatedNextUnboundedFrameIndexExpectedByPeer = nextUnboundedFrameIndexExpectedByPeer + difference
            logger.trace("Updating nextUnboundedFrameIndexExpectedByPeer from {} to {}", nextUnboundedFrameIndexExpectedByPeer, updatedNextUnboundedFrameIndexExpectedByPeer)
            nextUnboundedFrameIndexExpectedByPeer = updatedNextUnboundedFrameIndexExpectedByPeer
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