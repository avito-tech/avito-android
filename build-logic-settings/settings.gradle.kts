enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "build-logic-settings"

pluginManagement {
    // See rationale inside this script
    apply(from = "dependency-plugin/pluginManagement-shared.settings.gradle.kts")
}

val isInternalBuild = booleanProperty("avito.internalBuild", false)

if (!isInternalBuild) {
    logger.warn(
        """
        | -----------------------------------------
        | WARNING! 
        | This build doesn't use `avito.internalBuild`
        | 
        | For Avito employees only: it makes the build slower and less hermetic.
        | For external contributors: some artifacts might be not published yet to Maven Central.
        | -----------------------------------------
        """.trimMargin()
    )
}

dependencyResolutionManagement {

    @Suppress("UnstableApiUsage")
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

// HACK:
// We apply here the same settings as we provide by convention-cache plugin.
// Conventional way is to inherit build cache configuration from the root project into all included builds.
// The problem is - it doesn't work for included builds inside `pluginManagement`.
// They applied before the cache configuration in the root project itself.
// https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_composite
apply(from = "cache-plugin/src/main/kotlin/convention-cache.settings.gradle.kts")

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
