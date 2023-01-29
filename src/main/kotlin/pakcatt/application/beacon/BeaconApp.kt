package pakcatt.application.beacon

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pakcatt.application.beacon.model.BeaconAppConfig
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import java.util.*

@Component
class BeaconApp(private val myCall: String,
                private val beaconAppConfig: BeaconAppConfig): RootApp() {

    private var lastBeaconTimestamp: Long = 0

    override fun decisionOnConnectionRequest(request: AppRequest): AppResponse {
        return AppResponse.ignore()
    }

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: AppRequest, parsedCommandTokens: ParsedCommandTokens): AppResponse {
        return AppResponse.ignore()
    }

    @Scheduled(fixedDelay = 1000)
    private fun beacon() {
        val timestampNow = Date().time
        val intervalMilliseconds = beaconAppConfig.intervalMinutes() * 60000
        if (beaconAppConfig.intervalMinutes() > 0
            && lastBeaconTimestamp + intervalMilliseconds < timestampNow) {
            lastBeaconTimestamp = timestampNow
            for (channelIdentifier in beaconAppConfig.channelIdentifiers) {
                queueAdhocMessageForTransmission(
                    channelIdentifier,
                    beaconAppConfig.destination,
                    myCall,
                    beaconAppConfig.message,
                    DeliveryType.LINK_FIRE_AND_FORGET
                )
            }
        }
    }

}