package pakcatt.network.packet.link.model

import pakcatt.application.shared.SubApp

abstract class LinkResponse(private var responseString: String, private val nextApp: SubApp?) {

    fun nextApp(): SubApp? {
        return nextApp
    }

    fun responseString(): String {
        return responseString
    }

    fun updateResponseString(responseText: String) {
        responseString = responseText
    }

}