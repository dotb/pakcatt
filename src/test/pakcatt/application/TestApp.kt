package pakcatt.application

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.ConnectionResponse
import pakcatt.network.packet.link.model.InteractionResponse
import pakcatt.application.shared.RootApp

@Component
@Profile("test")
class TestApp: RootApp() {

    override fun returnCommandPrompt(): String {
        return "test>"
    }

    override fun decisionOnConnectionRequest(request: LinkRequest): ConnectionResponse {
        return ConnectionResponse.connect()
    }

    override fun handleReceivedMessage(request: LinkRequest): InteractionResponse {
        return InteractionResponse.sendText("Hi, there! *wave*")
    }
}