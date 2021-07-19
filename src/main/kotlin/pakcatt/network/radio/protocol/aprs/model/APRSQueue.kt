package pakcatt.network.radio.protocol.aprs.model

import org.springframework.stereotype.Component
import pakcatt.network.radio.kiss.model.KissFrame

@Component
class APRSQueue {

    private var deliveryQueue = ArrayList<KissFrame>()

    fun queueAPRSMessageForDelivery(sourceCallsign: String, destinationCallsign: String, message: String) {
        val aprsMessageFrame = APRSMessageFrame()
        aprsMessageFrame.setSourceCallsign(sourceCallsign)
        aprsMessageFrame.setMessageDestinationCallsign(destinationCallsign)
        aprsMessageFrame.setMessage(message)
        deliveryQueue.add(aprsMessageFrame)
    }

    fun flushQueue(): List<KissFrame> {
        val queueCopy = deliveryQueue
        deliveryQueue = ArrayList()
        return queueCopy
    }

}