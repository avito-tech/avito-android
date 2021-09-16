enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "avito-android-infra"

pluginManagement {
    includeBuild("build-logic-settings")

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

    val infraVersion = providers.systemProperty("infraVersion").forUseAtConfigurationTime()

    resolutionStrategy {
        eachPlugin {
            val pluginId = requested.id.id
            when {
                pluginId.startsWith("com.avito.android") ->
                    useModule("com.avito.android:${pluginId.removePrefix("com.avito.android.")}:${infraVersion.get()}")
            }
        }
    }
}

plugins {
    id("convention-scan")
    id("convention-cache")
    id("convention-dependencies")
}

includeBuild("build-logic")
includeBuild("build-logic-settings")
includeBuild("subprojects")
includeBuild("samples")
