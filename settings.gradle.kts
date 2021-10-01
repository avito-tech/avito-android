enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "avito-android-infra"

pluginManagement {
    includeBuild("build-logic-settings")
}

plugins {
    id("convention-plugins")
    id("convention-scan")
}

buildCache {
    remote<HttpBuildCache> {
        setUrl("http://localhost:5071/cache/") // local cache node to test
        isEnabled = true
        isPush = false
        isAllowUntrustedServer = true
        isAllowInsecureProtocol = true
    }
}

includeBuild("build-logic")
includeBuild("subprojects")
includeBuild("samples")
