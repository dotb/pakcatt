package pakcatt.network.radio.kiss.queue

import pakcatt.network.radio.kiss.model.KissFrame

class DeliveryQueue {

    private val deliveryQueue = ArrayList<KissFrame>()

    fun addFrame(frame: KissFrame) {
        deliveryQueue.add(frame)
    }

    fun allFrames(): List<KissFrame> {
        return deliveryQueue
    }

    fun queueSize(): Int {
        return deliveryQueue.size
    }

    fun setPFlagForFrameAtIndex(index: Int) {
        deliveryQueue[index].setPFlag()
    }

    fun unsetPFlagForFrameAtIndex(index: Int) {
        deliveryQueue[index].unsetPFlag()
    }

    fun addFramesFromQueue(incomingDeliveryQueue: DeliveryQueue) {
        for (frame in incomingDeliveryQueue.allFrames()) {
            deliveryQueue.add(frame)
        }
    }
}