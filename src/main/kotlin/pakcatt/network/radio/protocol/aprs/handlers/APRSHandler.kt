package pakcatt.network.radio.protocol.aprs.handlers

import pakcatt.application.shared.AppInterface
import pakcatt.network.radio.protocol.aprs.model.APRSDataType
import pakcatt.network.radio.protocol.aprs.model.APRSFrame
import pakcatt.network.radio.protocol.aprs.model.APRSQueue
import pakcatt.util.StringUtils

abstract class APRSHandler(val myCall: String,
                                val aprsQueue: APRSQueue,
                                val appInterface: AppInterface,
                                val stringUtils: StringUtils) {

    abstract fun isAbleToSupport(aprsDataType: APRSDataType): Boolean

    abstract fun handleAPRSFrame(aprsFrame: APRSFrame)

}