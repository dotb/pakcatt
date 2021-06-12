package pakcatt.dependencies

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import pakcatt.network.packet.tnc.TNC
import pakcatt.network.packet.tnc.TNCMocked

@Configuration
@Profile("test")
class Configuration {

    @Bean
    open fun serialPortPath(): String {
        return "/dev/null"
    }

    @Bean
    open fun serialPortBaud(): Int {
        return 9600
    }

    @Bean
    fun myCall(): String {
        return "VK3LIT-1"
    }

}