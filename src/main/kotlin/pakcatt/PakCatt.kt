package pakcatt

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableAsync
@ConfigurationPropertiesScan
open class PakCatt

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(PakCatt::class.java)
    runApplication<PakCatt>(*args)
}
