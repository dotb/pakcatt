package pakcatt.network.packet.protocol.no_layer_three.model

class LinkRequest(public val remoteCallsign: String,
                  public val addressedToCallsign: String,
                  public val message: String) {

}