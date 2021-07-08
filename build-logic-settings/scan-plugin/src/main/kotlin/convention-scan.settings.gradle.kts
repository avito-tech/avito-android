import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.version

plugins {
    id("com.gradle.enterprise")
}

val isCI = booleanProperty("ci", false)
val buildId = stringProperty("teamcityBuildId", nullIfBlank = true)

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
        // Lost scans due to upload interruptions after build finishes
        isUploadInBackground = false
        publishAlwaysIf(isCI)
        if (buildId != null) value("buildId", buildId)
    }
}

fun booleanProperty(name: String, defaultValue: Boolean): Boolean {
    return if (settings.extra.has(name)) {
        settings.extra[name]?.toString()?.toBoolean() ?: defaultValue
    } else {
        defaultValue
    }
}

fun stringProperty(name: String, nullIfBlank: Boolean = false): String? {
    return if (settings.extra.has(name)) {
        val string = settings.extra[name]?.toString()
        if (nullIfBlank && string.isNullOrBlank()) null else string
    } else {
        null
    }
}
