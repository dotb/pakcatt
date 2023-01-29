package pakcatt.network.radio.tnc.model

import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
data class TNCTCPConfig(val channel_identifier: String, val ip_address: String, val port: String)