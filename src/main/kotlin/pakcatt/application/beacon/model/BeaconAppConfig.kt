package pakcatt.application.beacon.model

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import pakcatt.network.radio.kiss.KissService

@ConstructorBinding
@ConfigurationProperties(prefix = "pakcatt.application.beacon")
data class BeaconAppConfig(
    val message: String,
    private val intervalMinutes: String,
    val destination: String,
    val channelIdentifiers: List<String>
) {

    private val logger = LoggerFactory.getLogger(KissService::class.java)

    fun intervalMinutes(): Int {
        return try {
            intervalMinutes.toInt()
        } catch (e: NumberFormatException) {
            logger.error("The beacon interval {} is not a valid number. Please configure pakcatt.application.beacon.interval_minutes using numeric characters.", intervalMinutes)
            0
        }
    }
}
