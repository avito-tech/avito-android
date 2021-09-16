enableFeaturePreview("VERSION_CATALOGS")

includeBuild("../build-logic")
includeBuild("../subprojects")

include(":test-runner")

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
        exclusiveContent {
            forRepository {
                maven {
                    if (artifactoryUrl.isNullOrBlank()) {
                        name = "mavenCentral"
                        setUrl("https://repo1.maven.org/maven2")
                    } else {
                        name = "Proxy for mavenCentral: https://repo1.maven.org/maven2"
                        setUrl("$artifactoryUrl/mavenCentral")
                        isAllowInsecureProtocol = true
                    }
                }
            }
            filter {
                includeGroup("com.avito.android")
            }
        }
        maven {
            if (artifactoryUrl.isNullOrBlank()) {
                name = "google-android"
                setUrl("https://dl.google.com/dl/android/maven2/")
            } else {
                name = "Proxy for google-android: https://dl.google.com/dl/android/maven2/"
                setUrl("$artifactoryUrl/google-android")
                isAllowInsecureProtocol = true
            }
        }
    }
}

plugins {
    id("convention-cache")
    id("convention-dependencies")
    id("convention-scan")
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
