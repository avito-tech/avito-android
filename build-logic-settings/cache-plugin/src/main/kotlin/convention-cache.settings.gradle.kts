import org.gradle.api.provider.Provider
import org.gradle.caching.http.HttpBuildCache
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.remote

@Suppress("UnstableApiUsage")
val avitoGithubRemoteCacheHost: Provider<String> = settings.providers
    .environmentVariable("GRADLE_CACHE_NODE_HOST")
    .forUseAtConfigurationTime()

val avitoGithubRemoteCachePush: String =
    extra.properties.getOrDefault("avitoGithub.gradle.buildCache.remote.push", "false").toString()

/**
 * Included builds will inherit this cache config
 * https://docs.gradle.org/current/userguide/build_cache.html#sec:build_cache_composite
 *
 * TODO it is not working as expected with --project-dir subprojects
 *  problem is that's how IDE runs tasks from included builds by default
 *  enabled only for "subprojects" for now
 */
buildCache {
    remote<HttpBuildCache> {
        setUrl("http://${avitoGithubRemoteCacheHost.orNull}/cache/")
        isEnabled = avitoGithubRemoteCacheHost.orNull != null
        isPush = avitoGithubRemoteCachePush.toBoolean()
        isAllowUntrustedServer = true
        isAllowInsecureProtocol = true
    }
}
