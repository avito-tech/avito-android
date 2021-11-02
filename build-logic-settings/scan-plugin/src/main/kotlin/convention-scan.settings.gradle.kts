import com.avito.booleanProperty
import com.avito.stringProperty

plugins {
    id("com.gradle.enterprise")
}

val isCi = booleanProperty("ci", false)
val publishBuildScan = booleanProperty("avito.gradle.buildScan.publishAlways", false)

val enterpriseUrl = stringProperty("avito.gradle.enterprise.url", nullIfBlank = true)

gradleEnterprise {
    if (!enterpriseUrl.isNullOrBlank()) {
        server = enterpriseUrl
        allowUntrustedServer = true

        buildScan {
            publishAlways()
        }
    } else {
        buildScan {
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
            publishAlwaysIf(publishBuildScan)
        }
    }

    buildScan {
        // Unstable in CI for unknown reasons
        // https://docs.gradle.com/enterprise/gradle-plugin/#failed_background_build_scan_uploads
        isUploadInBackground = !isCi
    }
}
