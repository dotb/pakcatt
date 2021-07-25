package pakcatt.application.shared.model

data class AppRequest(var remoteCallsign: String,
                      var remoteCallsignWithoutSSID: String,
                      var addressedToCallsign: String,
                      var message: String,
                      var viaRepeaterOne: String? = null,
                      var viaRepeaterTwo: String? = null,
                      var canReceiveMessage: Boolean = false,
                      var location: Location? = null)