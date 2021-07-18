package pakcatt.network.tcp

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.config.EnableIntegration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.ip.dsl.Tcp
import org.springframework.integration.ip.tcp.connection.AbstractConnectionFactory
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory
import org.springframework.integration.ip.tcp.connection.TcpNioServerConnectionFactory
import org.springframework.integration.ip.tcp.serializer.ByteArrayCrLfSerializer

@Configuration
@EnableIntegration
class TCPService {

    private val logger = LoggerFactory.getLogger(TCPService::class.java)
    private val tcpServerPort = 23

    @Bean
    fun commandServerFlow(): IntegrationFlow? {
        return IntegrationFlows.from(Tcp.inboundGateway(serverConnectionFactory()))
            .handle { payload: String, handlers ->
                logger.debug("TCP Input {}", payload)
            }
            .get()
    }

    fun serverConnectionFactory(): AbstractConnectionFactory {
        val tcpNetServerConnectionFactory = TcpNioServerConnectionFactory(tcpServerPort)
        tcpNetServerConnectionFactory.isSingleUse = true
        tcpNetServerConnectionFactory.serializer = codec()
        tcpNetServerConnectionFactory.deserializer = codec()
        return tcpNetServerConnectionFactory
    }

    fun codec(): ByteArrayCrLfSerializer {
        val crLfSerializer = ByteArrayCrLfSerializer()
        crLfSerializer.maxMessageSize = 204800000
        return crLfSerializer
    }
    
}