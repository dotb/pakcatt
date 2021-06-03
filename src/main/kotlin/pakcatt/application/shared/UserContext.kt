package pakcatt.application.shared

import java.util.*

class UserContext(remoteCallsign: String, myCallsign: String) {

    private var navigationStack = LinkedList<SubApp>()

    fun navigateToApp(app: SubApp) {
        navigationStack.push(app)
    }

    fun navigateBack() {
        navigationStack.pop()
    }

    fun engagedApplication(): SubApp? {
        return if (navigationStack.size > 0) {
            navigationStack.last
        } else {
            null
        }
    }

}