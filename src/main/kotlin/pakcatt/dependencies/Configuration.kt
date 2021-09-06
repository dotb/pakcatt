package pakcatt.dependencies

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import pakcatt.application.scriptable.model.Script
import pakcatt.application.scriptable.model.ScriptableConfig
import java.lang.NumberFormatException

@Configuration
@Profile("production")
class Configuration {

    val logger: Logger = LoggerFactory.getLogger(Configuration::class.java)

    @Value("\${pakcatt.application.mycall}")
    private lateinit var myCall: String
    @Bean
    fun myCall(): String {
        return myCall
    }

    @Value("\${pakcatt.application.welcomeMessage}")
    private lateinit var welcomeMessage: String
    @Bean
    fun welcomeMessage(): String {
        return welcomeMessage
    }

    @Value("\${pakcatt.application.defaultEndOfLine}")
    private lateinit var defaultEndOfLine: String
    @Bean
    fun defaultEndOfLine(): String {
        return defaultEndOfLine
    }

    @Value("\${pakcatt.application.board.summaryLength}")
    private lateinit var boardSummaryLength: String
    @Bean
    fun boardSummaryLength(): Int {
        return boardSummaryLength.toInt()
    }

    @Value("\${pakcatt.application.board.promptTopicLength}")
    private lateinit var boardPromptTopicLength: String
    @Bean
    fun boardPromptTopicLength(): Int {
        return boardPromptTopicLength.toInt()
    }

    @Value("\${pakcatt.application.board.defaultPostListLength}")
    private lateinit var defaultPostListLength: String
    @Bean
    fun boardPostListLength(): Int {
        return defaultPostListLength.toInt()
    }

    @Value("\${pakcatt.application.beacon.message}")
    private lateinit var beaconMessage: String
    @Bean
    fun beaconMessage(): String {
        return beaconMessage
    }

    @Value("\${pakcatt.application.beacon.interval_minutes}")
    private lateinit var beaconIntervalMinutes: String
    @Bean
    fun beaconIntervalMinutes(): Int {
        return try {
            beaconIntervalMinutes.toInt()
        } catch (e: NumberFormatException) {
            logger.error("The beacon interval {} is not a valid number. Please configure pakcatt.application.beacon.interval_minutes using numeric characters.", beaconIntervalMinutes)
            0
        }
    }

    @Value("\${pakcatt.application.beacon.destination}")
    private lateinit var beaconDestination: String
    @Bean
    fun beaconDestination(): String {
        return beaconDestination
    }

    @Value("\${pakcatt.application.startstop.send.startup.shutdown.messages}")
    private lateinit var sendStartupShutdownMessage: String
    @Bean
    fun sendStartupShutdownMessage(): Boolean {
        return sendStartupShutdownMessage.toBoolean()
    }

    @Value("\${pakcatt.application.startstop.startup.message}")
    private lateinit var startupMessage: String
    @Bean
    fun startupMessage(): String {
        return startupMessage
    }

    @Value("\${pakcatt.application.startstop.shutdown.message}")
    private lateinit var shutdownMessage: String
    @Bean
    fun shutdownMessage(): String {
        return shutdownMessage
    }

    @Autowired
    private lateinit var scriptableConfig: ScriptableConfig

    @Bean
    fun scriptWorkingDir(): String {
        return scriptableConfig.workingDir
    }

    @Bean
    fun scriptTimeout(): Long {
        return scriptableConfig.timeout
    }

    @Bean
    fun scriptableScripts(): List<Script> {
        return scriptableConfig.scripts
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

    @Value("\${pakcatt.network.packet.frame_size_max}")
    private lateinit var frameSizeMax: Number
    @Bean
    fun frameSizeMax(): Int {
        return frameSizeMax.toInt()
    }

    @Value("\${pakcatt.network.packet.frames_per_over}")
    private lateinit var framesPerOver: Number
    @Bean
    fun framesPerOver(): Int {
        return framesPerOver.toInt()
    }

    @Value("\${pakcatt.network.packet.minTXPauseSeconds}")
    private lateinit var minTXPauseSeconds: Number
    @Bean
    fun minTXPauseSeconds(): Int {
        return minTXPauseSeconds.toInt()
    }

    @Value("\${pakcatt.network.packet.maxDeliveryAttempts}")
    private lateinit var maxDeliveryAttempts: Number
    @Bean
    fun maxDeliveryAttempts(): Int {
        return maxDeliveryAttempts.toInt()
    }

    @Value("\${pakcatt.network.packet.deliveryRetryTimeSeconds}")
    private lateinit var deliveryRetryTimeSeconds: Number
    @Bean
    fun deliveryRetryTimeSeconds(): Int {
        return deliveryRetryTimeSeconds.toInt()
    }

}