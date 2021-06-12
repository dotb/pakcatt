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
    fun serialPortPath(): String {
        return "/dev/null"
    }

    @Bean
    fun serialPortBaud(): Int {
        return 9600
    }

}