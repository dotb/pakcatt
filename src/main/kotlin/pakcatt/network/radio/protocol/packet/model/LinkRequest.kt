package pakcatt.network.radio.protocol.packet.model

class LinkRequest(public val remoteCallsign: String,
                  public val addressedToCallsign: String,
                  public val message: String) {

}