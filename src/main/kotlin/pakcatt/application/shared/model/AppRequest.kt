package pakcatt.application.shared.model

import pakcatt.network.radio.protocol.aprs.model.APRSFrame

data class AppRequest(var remoteCallsign: String,
                      var addressedToCallsign: String,
                      var message: String,
                      var viaRepeaterOne: String? = null,
                      var viaRepeaterTwo: String? = null,
                      var canReceiveMessage: Boolean = false,
                      var location: Location? = null)