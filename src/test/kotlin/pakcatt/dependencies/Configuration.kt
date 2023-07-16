package pakcatt.dependencies

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class Configuration {

    @Bean
    fun myCall(): String {
        return "VK3LIT-1"
    }

    @Bean
    fun defaultEndOfLine(): String {
        return "\r\n"
    }

    @Bean
    fun sendStartupShutdownMessage(): Boolean {
        return true
    }

    @Bean
    fun startupMessage(): String {
        return "Startup message - hello!"
    }

    @Bean
    fun shutdownMessage(): String {
        return "Shutdown message - bye bye."
    }

    @Bean
    fun frameSizeMax(): Int {
        return 256
    }

    @Bean
    fun framesPerOver(): Int {
        return 2
    }

    @Bean
    fun minTXPauseSeconds(): Int {
        return 2
    }

    @Bean
    fun maxDeliveryAttempts(): Int {
        return 10
    }

    @Bean
    fun deliveryRetryTimeSeconds(): Int {
        return 10
    }

    @Bean
    fun tcpInteractiveEnabled(): Boolean {
        return true
    }

    @Bean
    fun tcpInteractivePort(): Int {
        return 3771
    }

    @Bean
    fun preWelcomeMessage(): String {
        return "Welcome be good and give me your callsign. K tnx."
    }

    @Bean
    fun regexForCallsignValidation(): String {
        return "[a-zA-Z]+[0-9][a-zA-Z]+"
    }

    @Bean
    fun callsignRegexFailMessage(): String {
        return "Go away."
    }

    @Bean
    fun conversationLogEnabled(): Boolean {
        return true
    }

    @Bean
    fun conversationLogPath(): String {
        return "/tmp"
    }

}