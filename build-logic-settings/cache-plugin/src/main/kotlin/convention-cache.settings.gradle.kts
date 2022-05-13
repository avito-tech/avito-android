/**
 * Can't use only one configuration for cache in the root project.
 * Included builds inside `pluginManagement` block can't reuse it,
 * because they are evaluated earlier than the build cache configuration in the root project.
 *
 * See https://github.com/gradle/gradle/issues/18511
 */
buildCache {
    local {
        isEnabled = booleanProperty("avito.gradle.buildCache.local.enabled", true)
        isPush = true
        removeUnusedEntriesAfterDays = 30
    }

    val isInternalBuild = booleanProperty("avito.internalBuild", false)

    val buildCacheUrl: String? = if (isInternalBuild) {
        val remoteUrl = checkNotNull(stringProperty("gradle.buildCache.remote.url", nullIfBlank = true)) {
            """
            Expected mandatory property `gradle.buildCache.remote.url` due to enabled `avito.internalBuild`.
            
            See https://avito-tech.github.io/avito-android/contributing/internal/RemoteCache
            """.trimIndent()
        }
        remoteUrl.removeSuffix("/").plus("/")
    } else {
        null
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
