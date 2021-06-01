package pakcatt.application

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import pakcatt.application.shared.AppRequest
import pakcatt.application.shared.ConnectionResponse
import pakcatt.application.shared.InteractionResponse
import pakcatt.application.shared.PacCattApp

@Component
@Profile("test")
class TestApp: PacCattApp() {
    override fun decisionOnConnectionRequest(request: AppRequest): ConnectionResponse {
        return ConnectionResponse.connect()
    }

    override fun handleReceivedMessage(request: AppRequest): InteractionResponse {
        return InteractionResponse.sendText("Hi, there! *wave*")
    }
}