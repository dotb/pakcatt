package pakcatt.application.tell.model

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "pakcatt.application.tell")
data class TellAppConfig(
    val channelIdentifiers: List<String>
)
