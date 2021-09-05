package pakcatt.application.filter.shared

import pakcatt.application.shared.model.AppRequest

abstract class InputFilter: Filter() {

    abstract fun applyFilter(request: AppRequest)

}