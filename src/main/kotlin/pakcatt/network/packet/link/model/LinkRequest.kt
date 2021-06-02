package pakcatt.network.packet.link.model

class LinkRequest(public val remoteCallsign: String,
                  public val addressedToCallsign: String,
                  public val message: String) {

}