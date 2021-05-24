package pakcatt.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class Configuration {

    @Value("\${pakcatt.service-name}")
    private lateinit var serviceName: String

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }

    @Bean
    open fun serviceName(): String {
        return serviceName
    }

}