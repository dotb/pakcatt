package pakcatt.network.packet.link.model

import pakcatt.application.shared.SubApp

abstract class LinkResponse(private val nextApp: SubApp?) {

    fun nextApp(): SubApp? {
        return nextApp
    }

}