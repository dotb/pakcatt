package pakcatt.application.shared.filter.common

import pakcatt.application.shared.UserContext
import pakcatt.application.shared.model.AppResponse

abstract class AppOutputFilter: Filter() {

    abstract fun applyFilter(response: AppResponse, userContext: UserContext?)

}