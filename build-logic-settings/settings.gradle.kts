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
@Suppress("UnstableApiUsage")
val avitoGithubRemoteCacheHost: Provider<String> = settings.providers
    .environmentVariable("GRADLE_CACHE_NODE_HOST")
    .forUseAtConfigurationTime()

val avitoGithubRemoteCachePush: String =
    extra.properties.getOrDefault("avitoGithub.gradle.buildCache.remote.push", "false").toString()

buildCache {
    remote<HttpBuildCache> {
        setUrl("http://${avitoGithubRemoteCacheHost.orNull}/cache/")
        isEnabled = avitoGithubRemoteCacheHost.orNull != null
        isPush = avitoGithubRemoteCachePush.toBoolean()
        isAllowUntrustedServer = true
        isAllowInsecureProtocol = true
    }
}

include("cache-plugin")
include("dependency-plugin")
include("scan-plugin")
