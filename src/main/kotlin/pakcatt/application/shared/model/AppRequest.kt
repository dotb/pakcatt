package pakcatt.application.shared.model

class AppRequest(public val remoteCallsign: String,
                 public val addressedToCallsign: String,
                 public val message: String) {

}