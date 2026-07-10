package com.avito.deeplink_generator

import com.avito.deeplink_generator.model.Deeplink
import com.avito.deeplink_generator.utils.assertContainsActivity
import com.avito.deeplink_generator.utils.assertContainsDeeplink
import com.avito.deeplink_generator.utils.assertContainsIntentFilter
import com.avito.deeplink_generator.utils.libModule
import com.avito.deeplink_generator.utils.validateManifest
import com.avito.deeplink_generator.utils.writeManifestWithUsesSdk
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class MergePublicDeeplinkManifestTest {

    @Test
    fun `assemble single library two times - manifest task is up to date`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(
                    buildScriptLinks = listOf(
                        "1/feed",
                        "1/profile"
                    ),
                    codeLinks = listOf(
                        "1/feed ru.avito",
                        "1/profile ru.avito"
                    )
                )
            ),
            gradleProperties = mapOf(
                "avito.deeplinks.filter.enabled" to "true",
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease")
            .assertThat()
            .taskWithOutcome(":lib:mergeReleasePublicDeeplinkManifest", TaskOutcome.SUCCESS)
        gradlew(projectDir, ":lib:assembleRelease")
            .assertThat()
            .taskWithOutcome(":lib:mergeReleasePublicDeeplinkManifest", TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun `assemble single library without deeplinks - manifest with deeplinks is not generated`(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(buildScriptLinks = emptyList(), codeLinks = emptyList())
            ),
            gradleProperties = mapOf(
                "avito.deeplinks.filter.enabled" to "true",
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease").assertThat()
            .buildSuccessful()
            .tasksShouldNotBeTriggered(":lib:mergeReleasePublicDeeplinkManifest")
    }

    @Test
    fun `assemble single library - manifest generated correctly`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule()
            ),
            gradleProperties = mapOf(
                "avito.deeplinks.filter.enabled" to "true",
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            manifestProjectDir = File(projectDir, "lib"),
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsIntentFilter()
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/feed"))
            }
        )
    }

    @Test
    fun `assemble single library with uses-sdk in manifest - merge succeeds`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(mutator = { writeManifestWithUsesSdk() })
            ),
            gradleProperties = mapOf(
                "avito.deeplinks.filter.enabled" to "true",
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            manifestProjectDir = File(projectDir, "lib"),
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/feed"))
            }
        )
    }

    @Test
    fun `assemble single library with multi-regional deeplinks - manifest generated correctly`(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(
                    buildScriptLinks = listOf("1/feed"),
                    codeLinks = listOf("1/feed ru.avito,com.scheme"),
                )
            ),
            gradleProperties = mapOf(
                "avito.deeplinks.filter.enabled" to "true",
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            manifestProjectDir = File(projectDir, "lib"),
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsIntentFilter(count = 1)
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/feed"), dataPosition = 1)
                manifest.assertContainsDeeplink(Deeplink("com.scheme", "1", "/feed"), dataPosition = 2)
            }
        )
    }

    @Test
    fun `assemble single library with different hosts - manifest divided by intent filter blocks`(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                libModule(
                    buildScriptLinks = listOf("1/feed", "2/feed"),
                    codeLinks = listOf("1/feed ru.avito", "2/feed ru.avito"),
                )
            ),
            gradleProperties = mapOf(
                "avito.deeplinks.filter.enabled" to "true",
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":lib:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            manifestProjectDir = File(projectDir, "lib"),
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsIntentFilter(count = 2)
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/feed"), filterPosition = 1)
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "2", "/feed"), filterPosition = 2)
            }
        )
    }

    @Test
    fun `assemble app with several libraries - app manifest contains merged information about links`(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    dependencies = setOf(
                        project(":feed"),
                        project(":profile")
                    )
                ),
                libModule(name = "feed", buildScriptLinks = listOf("1/feed"), codeLinks = listOf("1/feed ru.avito")),
                libModule(
                    name = "profile",
                    buildScriptLinks = listOf("1/profile"),
                    codeLinks = listOf("1/profile ru.avito")
                )
            ),
            gradleProperties = mapOf(
                "avito.deeplinks.filter.enabled" to "true",
            )
        ).generateIn(projectDir)

        gradlew(projectDir, ":app:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            manifestProjectDir = File(projectDir, "app"),
            isForLib = false,
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/feed"), filterPosition = 1)
                manifest.assertContainsDeeplink(Deeplink("ru.avito", "1", "/profile"), filterPosition = 2)
            }
        )
    }
}
