package pakcatt.application.tell.model

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "pakcatt.application.tell")
data class TellAppConfig(
    val channelIdentifiers: List<String>
)
