package pakcatt.application.settings.persistence

import org.apache.catalina.User
import org.springframework.stereotype.Component
import pakcatt.application.last.persistence.LastEntry
import pakcatt.application.last.persistence.LastEntryRepository
import java.util.*

@Component
class SettingStore(private val settingsRepository: SettingsRepository) {

    fun saveSettings(newSetting: UserSetting) {
        var existingSetting = settingsRepository.findBySettingOwnerAndKey(newSetting.settingOwner, newSetting.key)
        if (null == existingSetting) {
            settingsRepository.insert(newSetting)
        } else {
            settingsRepository.save(newSetting)
        }
    }

    fun getSettingsForUser(callSign: String): List<UserSetting> {
        return settingsRepository.findBySettingOwner(callSign)
    }
}