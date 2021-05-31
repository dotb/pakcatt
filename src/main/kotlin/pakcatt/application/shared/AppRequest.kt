package pakcatt.application.shared

class AppRequest(public val remoteCallsign: String,
                 public val addressedToCallsign: String,
                 public val message: String) {

}