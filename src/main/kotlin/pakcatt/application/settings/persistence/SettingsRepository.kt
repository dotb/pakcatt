package pakcatt.application.settings.persistence

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Service
import pakcatt.application.mailbox.persistence.MailMessage

@Service
interface SettingsRepository: MongoRepository<UserSetting, Int> {
    fun findBySettingOwnerAndKey(settingOwner: String, key: String): List<UserSetting>
}
