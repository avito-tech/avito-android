enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "build-logic-settings"

pluginManagement {

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

    val artifactoryUrl: String? by settings

    repositories {
        exclusiveContent {
            forRepository {
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
            filter {
                includeGroupByRegex("com\\.gradle.*")
                includeGroupByRegex("org\\.jetbrains.*")
            }
        }
    }
}

include("cache-plugin")
include("dependency-plugin")
include("scan-plugin")
