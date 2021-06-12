package pakcatt.network.packet.link.model

import pakcatt.application.shared.SubApp

class LinkRequest(public val remoteCallsign: String,
                  public val addressedToCallsign: String,
                  public val message: String) {

}