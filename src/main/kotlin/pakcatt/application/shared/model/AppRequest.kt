package pakcatt.application.shared.model

import pakcatt.application.shared.UserContext

data class AppRequest(var channelIdentifier: String,
                      var remoteCallsign: String,
                      var remoteCallsignWithoutSSID: String,
                      var addressedToCallsign: String,
                      var message: String,
                      var viaRepeaterOne: String? = null,
                      var viaRepeaterTwo: String? = null,
                      var canReceiveMessage: Boolean = false,
                      var location: Location? = null,
                      var userContext: UserContext? = null)