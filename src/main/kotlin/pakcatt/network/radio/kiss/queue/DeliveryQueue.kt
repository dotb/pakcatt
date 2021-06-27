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

}