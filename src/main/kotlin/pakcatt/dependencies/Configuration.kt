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
import pakcatt.network.radio.tnc.TNC
import pakcatt.network.radio.tnc.TNCDataStreamSerial
import pakcatt.network.radio.tnc.TNCDataStreamTCP
import pakcatt.network.radio.tnc.model.TNCConfig
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

    @Value("\${pakcatt.application.welcome-message}")
    private lateinit var welcomeMessage: String
    @Bean
    fun welcomeMessage(): String {
        return welcomeMessage
    }

    @Value("\${pakcatt.application.default-end-of-line}")
    private lateinit var defaultEndOfLine: String
    @Bean
    fun defaultEndOfLine(): String {
        return defaultEndOfLine
    }

    @Value("\${pakcatt.application.board.summary-length}")
    private lateinit var boardSummaryLength: String
    @Bean
    fun boardSummaryLength(): Int {
        return boardSummaryLength.toInt()
    }

    @Value("\${pakcatt.application.board.prompt-topic-length}")
    private lateinit var boardPromptTopicLength: String
    @Bean
    fun boardPromptTopicLength(): Int {
        return boardPromptTopicLength.toInt()
    }

    @Value("\${pakcatt.application.board.default-post-list-length}")
    private lateinit var defaultPostListLength: String
    @Bean
    fun boardPostListLength(): Int {
        return defaultPostListLength.toInt()
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

    @Autowired
    private lateinit var tncConfig: TNCConfig
    @Bean
    fun tncConnections(): List<TNC> {
        val consolidatedTNCList = mutableListOf<TNC>()
        for (tncConfig in tncConfig.serial_connections) {
            consolidatedTNCList.add(TNCDataStreamSerial(tncConfig.channel_identifier, tncConfig.port_path, tncConfig.port_baud.toInt()))
        }
        for (tncConfig in tncConfig.tcp_connections) {
            consolidatedTNCList.add(TNCDataStreamTCP(tncConfig.channel_identifier, tncConfig.ip_address, tncConfig.port.toInt()))
        }
        return consolidatedTNCList
    }

    @Value("\${pakcatt.network.packet.frame-size-max}")
    private lateinit var frameSizeMax: Number
    @Bean
    fun frameSizeMax(): Int {
        return frameSizeMax.toInt()
    }

    @Value("\${pakcatt.network.packet.frames-per-over}")
    private lateinit var framesPerOver: Number
    @Bean
    fun framesPerOver(): Int {
        return framesPerOver.toInt()
    }

    @Value("\${pakcatt.network.packet.min-tx-pause-seconds}")
    private lateinit var minTXPauseSeconds: Number
    @Bean
    fun minTXPauseSeconds(): Int {
        return minTXPauseSeconds.toInt()
    }

    @Value("\${pakcatt.network.packet.max-delivery-attempts}")
    private lateinit var maxDeliveryAttempts: Number
    @Bean
    fun maxDeliveryAttempts(): Int {
        return maxDeliveryAttempts.toInt()
    }

    @Value("\${pakcatt.network.packet.delivery-retry-time-seconds}")
    private lateinit var deliveryRetryTimeSeconds: Number
    @Bean
    fun deliveryRetryTimeSeconds(): Int {
        return deliveryRetryTimeSeconds.toInt()
    }

}