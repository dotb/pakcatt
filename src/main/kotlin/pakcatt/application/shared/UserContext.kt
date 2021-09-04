package pakcatt.application.shared

import pakcatt.util.StringUtils
import java.util.*

class UserContext(val remoteCallsign: String,
                  val myCallsign: String) {

    private var navigationStack = LinkedList<SubApp>()
    var eolSequence = StringUtils().EOL

    fun navigateToApp(app: SubApp) {
        navigationStack.push(app)
    }

    fun navigateBack() {
        if (navigationStack.size >= 2) {
            navigationStack.pop()
        }
    }

    fun engagedApplication(): SubApp? {
        return if (navigationStack.size > 0) {
            navigationStack.first
        } else {
            null
        }
    }

    fun rootApplication(): RootApp? {
        return if (navigationStack.isNotEmpty()) {
            navigationStack.first as? RootApp
        } else {
            null
        }
    }

}