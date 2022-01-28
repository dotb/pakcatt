package pakcatt.application.shared.model

import pakcatt.application.shared.UserContext

data class AppRequest(var remoteCallsign: String,
                      var remoteCallsignWithoutSSID: String,
                      var addressedToCallsign: String,
                      var message: String,
                      var channelIsInteractive: Boolean, // Is the user sitting at a terminal (true), or are they on an async messaging device (false)
                      var viaRepeaterOne: String? = null,
                      var viaRepeaterTwo: String? = null,
                      var canReceiveMessage: Boolean = false,
                      var location: Location? = null,
                      var userContext: UserContext? = null)