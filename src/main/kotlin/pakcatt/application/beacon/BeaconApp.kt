package pakcatt.application.beacon

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import pakcatt.application.shared.RootApp
import pakcatt.network.packet.link.model.DeliveryType
import pakcatt.network.packet.link.model.LinkRequest
import pakcatt.network.packet.link.model.LinkResponse
import java.util.*

@Component
class BeaconApp(private val myCall: String,
                private val beaconMessage: String,
                private val beaconIntervalMinutes: Int,
                private val beaconDestination: String): RootApp() {

    private var lastBeaconTimestamp: Long = 0

    override fun decisionOnConnectionRequest(request: LinkRequest): LinkResponse {
        return LinkResponse.ignore()
    }

    override fun returnCommandPrompt(): String {
        return ""
    }

    override fun handleReceivedMessage(request: LinkRequest): LinkResponse {
        return LinkResponse.ignore()
    }

    @Scheduled(fixedDelay = 1000)
    private fun beacon() {
        val timestampNow = Date().time
        val intervalMilliseconds = beaconIntervalMinutes * 60000
        if (lastBeaconTimestamp + intervalMilliseconds < timestampNow) {
            queueAdhocMessageForTransmission(beaconDestination, myCall, beaconMessage, DeliveryType.FIRE_AND_FORGET)
            lastBeaconTimestamp = timestampNow
        }
    }

}