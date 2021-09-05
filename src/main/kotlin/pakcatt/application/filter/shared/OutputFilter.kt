package pakcatt.application.filter.shared

import pakcatt.application.shared.UserContext
import pakcatt.application.shared.model.AppResponse

abstract class OutputFilter: Filter() {

    abstract fun applyFilter(response: AppResponse, userContext: UserContext?)

}