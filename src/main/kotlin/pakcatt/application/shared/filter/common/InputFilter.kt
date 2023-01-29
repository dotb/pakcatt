package pakcatt.application.shared.filter.common

import pakcatt.application.shared.model.AppRequest

abstract class InputFilter: Filter() {

    abstract fun applyFilter(request: AppRequest)

}