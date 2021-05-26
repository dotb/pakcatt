package pakcatt.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import pakcatt.tnc.TNC
import pakcatt.tnc.TNCSerial

@Configuration
class Configuration {

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