enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "avito-android-infra"

pluginManagement {
    includeBuild("build-logic-settings")
}

plugins {
    id("convention-plugins")
    id("convention-scan")
    id("convention-cache")
}

includeBuild("build-logic")
includeBuild("subprojects")
includeBuild("samples")
