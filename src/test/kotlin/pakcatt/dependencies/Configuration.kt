package pakcatt.dependencies

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

}