package pakcatt.application.shared.model

data class AppRequest(public val remoteCallsign: String,
                 public val addressedToCallsign: String,
                 public val content: String,
                 public val location: Location? = null)