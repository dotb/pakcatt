package pakcatt.network.radio.protocol.aprs.handlers

import pakcatt.application.shared.AppInterface
import pakcatt.network.radio.protocol.aprs.model.APRSFrame
import pakcatt.network.radio.protocol.aprs.model.APRSMicEDataFrame
import pakcatt.network.radio.protocol.aprs.model.APRSQueue
import pakcatt.util.StringUtils

class APRSMicEDataHandler(myCall: String,
                          aprsQueue: APRSQueue,
                          appInterface: AppInterface,
                          stringUtils: StringUtils
): APRSHandler(myCall, aprsQueue, appInterface, stringUtils) {

    override fun handleAPRSFrame(aprsFrame: APRSFrame) {
        val micEDataFrame = aprsFrame as APRSMicEDataFrame

    }

}