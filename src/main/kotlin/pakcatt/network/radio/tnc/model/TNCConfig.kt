package pakcatt.network.radio.tnc.model

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "pakcatt.tnc")
data class TNCConfig(val serial_connections: List<TNCSerialConfig>, val tcp_connections: List<TNCTCPConfig>)