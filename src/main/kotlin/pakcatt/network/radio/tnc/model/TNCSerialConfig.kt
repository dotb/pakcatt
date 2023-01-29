package pakcatt.network.radio.tnc.model

import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
data class TNCSerialConfig(val channel_identifier: String, val port_path: String, val port_baud: String)