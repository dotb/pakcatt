package pakcatt.dependencies

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import pakcatt.application.scriptable.model.Script
import pakcatt.application.scriptable.model.ScriptableConfig

@Configuration
@Profile("test")
class Configuration {

    @Bean
    fun myCall(): String {
        return "VK3LIT-1"
    }

    @Bean
    fun beaconMessage(): String {
        return "Beacon message"
    }

    @Bean
    fun beaconIntervalMinutes(): Int {
        return 1
    }

    @Bean
    fun beaconDestination(): String {
        return "CQ"
    }

    @Bean
    fun scriptWorkingDir(): String {
        return "scripts/"
    }

    @Bean
    fun scriptTimeout(): Long {
        return 2
    }

    @Bean
    fun scriptableScripts(): List<Script> {
        return listOf(Script("connect.sh", "example_date_connect.sh", "example_date_prompt.sh", "example_date_request.sh"))
    }

    @Bean
    fun serialPortPath(): String {
        return "/dev/null"
    }

    @Bean
    fun serialPortBaud(): Int {
        return 9600
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