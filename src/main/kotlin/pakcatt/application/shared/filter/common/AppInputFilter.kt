package pakcatt.application.shared.filter.common

import pakcatt.application.shared.model.AppRequest

abstract class AppInputFilter: Filter() {

    abstract fun applyFilter(request: AppRequest)

}