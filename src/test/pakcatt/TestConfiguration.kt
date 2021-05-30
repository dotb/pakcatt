package pakcatt

import org.springframework.context.annotation.*
import pakcatt.network.packet.tnc.TNC
import pakcatt.network.packet.tnc.TNCMocked

@Configuration
@Profile("test")
class TestConfiguration {

    @Bean
    @Primary
    fun tnc(): TNC {
        return TNCMocked()
    }

    @Bean
    fun myCall(): String {
        return "VK3LIT-1"
    }
    @Bean
    open fun serviceName(): String {
        return "PacCatt"
    }

    @Bean
    open fun serialPortPath(): String {
        return "/dev/null"
    }

    @Bean
    open fun serialPortBaud(): Int {
        return 9600
    }

}