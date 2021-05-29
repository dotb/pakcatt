package pakcatt.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class Configuration {

    @Value("\${pakcatt.mycall}")
    private lateinit var myCall: String

    @Value("\${pakcatt.service-name}")
    private lateinit var serviceName: String

    @Value("\${pakcatt.serial-port-path}")
    private lateinit var serialPortPath: String

    @Value("\${pakcatt.serial-port-baud}")
    private lateinit var serialPortBaud: Number

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    open fun myCall(): String {
        return myCall
    }

    @Bean
    open fun serviceName(): String {
        return serviceName
    }

    @Bean
    open fun serialPortPath(): String {
        return serialPortPath
    }

    @Bean
    open fun serialPortBaud(): Int {
        return serialPortBaud.toInt()
    }

}