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

// Duplicated settings because they are not inherited from root project
// as described in https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_composite
// https://github.com/gradle/gradle/issues/18511
val enterpriseUrl = stringProperty("avito.gradle.enterprise.url", nullIfBlank = true)

buildCache {
    local {
        isEnabled = booleanProperty("avito.gradle.buildCache.local.enabled", true)
        isPush = true
        removeUnusedEntriesAfterDays = 30
    }
    if (!enterpriseUrl.isNullOrBlank()) {
        remote<HttpBuildCache> {
            setUrl("$enterpriseUrl/cache/")
            isEnabled = true
            isPush = booleanProperty("avito.gradle.buildCache.remote.push", false)
            isAllowUntrustedServer = true
            isAllowInsecureProtocol = true
        }
    }
}

include("cache-plugin")
include("dependency-plugin")
include("scan-plugin")
include("extensions")

fun booleanProperty(name: String, defaultValue: Boolean): Boolean {
    return if (settings.extra.has(name)) {
        settings.extra[name]?.toString()?.toBoolean() ?: defaultValue
    } else {
        defaultValue
    }
}

fun Settings.stringProperty(name: String, nullIfBlank: Boolean = false): String? {
    return if (extra.has(name)) {
        val string = extra[name]?.toString()
        if (nullIfBlank && string.isNullOrBlank()) null else string
    } else {
        null
    }
}
