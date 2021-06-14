package pakcatt.network.packet.kiss.queue

import pakcatt.network.packet.kiss.model.KissFrame

class DeliveryQueue {

    private val deliveryQueue = ArrayList<KissFrame>()

    fun addFrame(frame: KissFrame) {
        deliveryQueue.add(frame)
    }

    fun allFrames(): List<KissFrame> {
        return deliveryQueue
    }

}