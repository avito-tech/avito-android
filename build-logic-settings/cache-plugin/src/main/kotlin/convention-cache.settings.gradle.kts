
import com.avito.booleanProperty
import com.avito.stringProperty

// Can't use only one configuration for cache in the root project.
// Included builds inside `pluginManagement` block can't reuse it,
// because they are evaluated earlier than the build cache configuration in the root project.
//
// see https://github.com/gradle/gradle/issues/18511
// Duplicated in settings in parent project

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
    } else {
        gradle.buildFinished {
            logger.warn(
                "" +
                    "-----------------------------------------\n" +
                    "| WARNING! (for Avito employees only)\n" +
                    "| Gradle Remote Build cache is disabled\n" +
                    "| Build performance could be much better\n" +
                    "| See https://avito-tech.github.io/avito-android/contributing/internal/RemoteCache/\n" +
                    "-----------------------------------------\n"
            )
        }
    }
}
