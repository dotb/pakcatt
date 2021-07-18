package pakcatt.application.scriptable.model

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "pakcatt.application.scriptable")
data class ScriptableConfig(val workingDir: String, val timeout: Long, val scripts: List<Script>)
