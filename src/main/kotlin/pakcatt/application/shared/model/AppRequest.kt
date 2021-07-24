package pakcatt.application.shared.model

import pakcatt.network.radio.protocol.aprs.model.APRSFrame

data class AppRequest(var remoteCallsign: String,
                      var addressedToCallsign: String,
                      var content: String,
                      var remoteStationCanReceiveResponse: Boolean = false,
                      var location: Location? = null)