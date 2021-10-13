package pakcatt.application.settings

import pakcatt.application.settings.persistence.SettingStore
import pakcatt.application.settings.persistence.UserSetting
import pakcatt.application.shared.FORMAT
import pakcatt.application.shared.NavigateBack
import pakcatt.application.shared.SubApp
import pakcatt.application.shared.command.Command
import pakcatt.application.shared.model.AppRequest
import pakcatt.application.shared.model.AppResponse
import java.lang.StringBuilder

class SettingsApp(val settingStore: SettingStore): SubApp() {

    init {
        registerCommand(Command("list") .function { listSettings(it) }  .description("List settings"))
        registerCommand(Command("back") .reply("Bye").openApp(NavigateBack(1)).description("Return to the main menu"))
        registerCommand(Command("set") .function { setSetting(it) } .description("Make setting"))
    }

    override fun returnCommandPrompt(): String {
        return "settings>"
    }

    override fun handleReceivedMessage(request: AppRequest): AppResponse {
        return handleRequestWithRegisteredCommand(request)
    }

    private fun listSettings(request: AppRequest): AppResponse {
        val userContext = request.userContext
        return if (null != userContext) {
            AppResponse.sendText(compileListOfSettings(request))
        } else {
            AppResponse.sendText("No configuration set")
        }
    }

    private fun compileListOfSettings(request: AppRequest): String {
        // Get settings from repository, selecting by user call sign
        val callSign = stringUtils.formatCallsignRemoveSSID(request.remoteCallsign)
        val settings = settingStore.getSettingsForUser(callSign)

        // Heading for rows to be outputted below
        val settingResponse = StringBuilder()
        settingResponse.append(stringUtils.EOL)
        settingResponse.append(textFormat.format(FORMAT.BOLD))
        settingResponse.append("Setting Name${tabSpace}Is User-configurable?${tabSpace}Setting Value")
        settingResponse.append(textFormat.format(FORMAT.RESET))
        settingResponse.append(stringUtils.EOL)

        for(setting in settings) {
            val userConfigurable = if (setting.userConfigurable) "Y" else "N"
            settingResponse.append("${setting.key}${tabSpace}${userConfigurable}${tabSpace}${setting.value}")
        }

        return settingResponse.toString()
    }

    private fun setSetting(request: AppRequest): AppResponse {
        // Check that value is allowed
        // To be implemented

        // Get key and value to be set
        val settingName = parseStringArguments(request.message, "")[1]
        val settingValue = parseStringArguments(request.message, "")[2]

        // Put these in the database
        val callSign = request.remoteCallsign
        val setting = UserSetting(settingName, settingValue, emptyList(), true, callSign)
        settingStore.saveSettings(setting)

        // Set these in the user context
        // To be implemented

        return AppResponse.sendText("Setting saved")
    }

}