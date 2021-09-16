rootProject.name = "build-logic"

enableFeaturePreview("VERSION_CATALOGS")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("../build-logic-settings")

    val artifactoryUrl: String? by settings

    repositories {
        maven {
            if (artifactoryUrl.isNullOrBlank()) {
                name = "gradle-plugins"
                setUrl("https://plugins.gradle.org/m2/")
            } else {
                name = "Proxy for gradle-plugins: https://plugins.gradle.org/m2/"
                setUrl("$artifactoryUrl/gradle-plugins")
                isAllowInsecureProtocol = true
            }
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

plugins {
    id("convention-dependencies")
}

include("kotlin")
include("android")
include("testing")
include("checks")
include("gradle")
include("publication")
