package pakcatt.application.settings.persistence

import org.springframework.data.annotation.Id

data class UserSetting(val key: String,
                       val value: String = "",
                       val allowedValues: List<String>,
                       val userConfigurable: Boolean,
                       val settingOwner: String = "",
                       @Id val uid: Int = 0
)