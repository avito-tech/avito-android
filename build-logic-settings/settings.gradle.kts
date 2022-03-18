enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "build-logic-settings"

pluginManagement {

    fun Settings.booleanProperty(name: String, defaultValue: Boolean): Boolean {
        return if (extra.has(name)) {
            extra[name]?.toString()?.toBoolean() ?: defaultValue
        } else {
            defaultValue
        }
    }

    val isInternalBuild = booleanProperty("avito.internalBuild", true)

    repositories {
        maven {
            if (isInternalBuild) {
                val artifactoryUrl: String? by settings
                require(!artifactoryUrl.isNullOrBlank()) {
                    "artifactoryUrl should be set for avito.internalBuild=true"
                }
                name = "Proxy for gradle-plugins: https://plugins.gradle.org/m2/"
                setUrl("$artifactoryUrl/gradle-plugins")
                isAllowInsecureProtocol = true
            } else {
                name = "gradle-plugins"
                setUrl("https://plugins.gradle.org/m2/")
            }
        }
    }
}

dependencyResolutionManagement {

    val isInternalBuild = booleanProperty("avito.internalBuild", true)

    repositories {
        exclusiveContent {
            forRepository {
                maven {
                    if (isInternalBuild) {
                        val artifactoryUrl: String by settings
                        name = "Proxy for gradle-plugins: https://plugins.gradle.org/m2/"
                        setUrl("$artifactoryUrl/gradle-plugins")
                        isAllowInsecureProtocol = true
                    } else {
                        name = "gradle-plugins"
                        setUrl("https://plugins.gradle.org/m2/")
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
val buildCacheUrl = stringProperty("gradle.buildCache.remote.url", nullIfBlank = true)
    ?.removeSuffix("/")
    ?.plus("/cache")

buildCache {
    local {
        isEnabled = booleanProperty("avito.gradle.buildCache.local.enabled", true)
        isPush = true
        removeUnusedEntriesAfterDays = 30
    }
    if (!buildCacheUrl.isNullOrBlank()) {
        remote<HttpBuildCache> {
            setUrl(buildCacheUrl)
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

fun Settings.booleanProperty(name: String, defaultValue: Boolean): Boolean {
    return if (extra.has(name)) {
        extra[name]?.toString()?.toBoolean() ?: defaultValue
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
