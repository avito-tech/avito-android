// See also duplicated settings in parent project
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
