enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "avito-android-infra"

includeBuild("build-logic")
includeBuild("build-logic-settings")
includeBuild("subprojects")
includeBuild("samples")
