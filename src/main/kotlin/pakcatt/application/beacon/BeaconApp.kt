package pakcatt.application.beacon

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pakcatt.application.shared.RootApp
import pakcatt.application.shared.model.DeliveryType
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import pakcatt.application.shared.model.ParsedCommandTokens
import java.util.*

@Component
class BeaconApp(private val myCall: String,
                private val beaconMessage: String,
                private val beaconIntervalMinutes: Int,
                private val beaconDestination: String): RootApp() {

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
        val intervalMilliseconds = beaconIntervalMinutes * 60000
        if (beaconIntervalMinutes > 0
            && lastBeaconTimestamp + intervalMilliseconds < timestampNow) {
            queueAdhocMessageForTransmission(beaconDestination, myCall, beaconMessage, DeliveryType.LINK_FIRE_AND_FORGET)
            lastBeaconTimestamp = timestampNow
        }
    }

}