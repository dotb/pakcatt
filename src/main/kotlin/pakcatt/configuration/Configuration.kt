package pakcatt.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestTemplate

@Configuration
@Profile("production")
class Configuration {

    @Value("\${pakcatt.serial-port-path}")
    private lateinit var serialPortPath: String

    @Value("\${pakcatt.serial-port-baud}")
    private lateinit var serialPortBaud: Number

    @Value("\${pakcatt.application.simple-test.mycall}")
    private lateinit var simpleTestMyCall: String

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    open fun serialPortPath(): String {
        return serialPortPath
    }

    @Bean
    open fun serialPortBaud(): Int {
        return serialPortBaud.toInt()
    }

    @Bean
    open fun simpleTestMyCall(): String {
        return simpleTestMyCall
    }

}