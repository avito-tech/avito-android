import com.avito.booleanProperty
import com.avito.stringProperty

plugins {
    id("com.gradle.enterprise")
}

val isCi = booleanProperty("ci", false)
val publishBuildScan = booleanProperty("avito.gradle.buildScan.publish", false)

val buildCacheUrl = stringProperty("gradle.buildCache.remote.url", nullIfBlank = true)
    ?.removeSuffix("/")
    ?.plus("/cache")

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"

        publishAlwaysIf(publishBuildScan)

        // Unstable in CI for unknown reasons
        // https://docs.gradle.com/enterprise/gradle-plugin/#failed_background_build_scan_uploads
        isUploadInBackground = !isCi
    }
}
