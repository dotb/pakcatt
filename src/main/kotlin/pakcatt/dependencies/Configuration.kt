package pakcatt.dependencies

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.lang.NumberFormatException

@Configuration
@Profile("production")
class Configuration {

    val logger = LoggerFactory.getLogger(Configuration::class.java)

    @Value("\${pakcatt.application.mycall}")
    private lateinit var myCall: String
    @Bean
    fun myCall(): String {
        return myCall
    }

    @Value("\${pakcatt.application.beacon.message}")
    private lateinit var beaconMessage: String
    @Bean
    fun beaconMessage(): String {
        return beaconMessage
    }

    @Value("\${pakcatt.application.beacon.interval_seconds}")
    private lateinit var beaconIntervalSeconds: String
    @Bean
    fun beaconIntervalSeconds(): Int {
        return try {
            beaconIntervalSeconds.toInt()
        } catch (e: NumberFormatException) {
            logger.error("The beacon interval {} is not a valid number. Please configure pakcatt.application.beacon.interval_seconds using numeric characters.", beaconIntervalSeconds)
            0
        }
    }


    @Value("\${pakcatt.serial-port-path}")
    private lateinit var serialPortPath: String
    @Bean
    fun serialPortPath(): String {
        return serialPortPath
    }


    @Value("\${pakcatt.serial-port-baud}")
    private lateinit var serialPortBaud: Number
    @Bean
    fun serialPortBaud(): Int {
        return serialPortBaud.toInt()
    }

}