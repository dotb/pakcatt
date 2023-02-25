package pakcatt.util

import java.util.*

class SimpleTimer(private val delaySeconds: Int) {

    private var lastTriggerTimeStamp: Long = 0

    fun hasExpired(): Boolean {
        val timerTimeInSeconds = delaySeconds * 1000
        return Date().time - timerTimeInSeconds > lastTriggerTimeStamp
    }

    fun reset() {
        lastTriggerTimeStamp = Date().time
    }

    fun expireTimer() {
        lastTriggerTimeStamp = 0
    }

}