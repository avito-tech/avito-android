package com.avito.deeplink_generator

import com.avito.deeplink_generator.model.Deeplink
import com.avito.deeplink_generator.utils.assertContainsActivity
import com.avito.deeplink_generator.utils.assertContainsDeeplink
import com.avito.deeplink_generator.utils.assertContainsIntentFilter
import com.avito.deeplink_generator.utils.assertDeeplinkCount
import com.avito.deeplink_generator.utils.libModule
import com.avito.deeplink_generator.utils.validateManifest
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class DeeplinkManifestFilterTest {

    @Test
    fun `assemble app two times - manifest filter task is up to date`(@TempDir projectDir: File) {
        generateTestProject(projectDir)

        gradlew(projectDir, ":app:assembleRelease")
            .assertThat()
            .taskWithOutcome(":app:filterReleasePublicDeeplinkManifest", TaskOutcome.SUCCESS)
        gradlew(projectDir, ":app:assembleRelease")
            .assertThat()
            .taskWithOutcome(":app:filterReleasePublicDeeplinkManifest", TaskOutcome.UP_TO_DATE)
    }

    @Test
    fun `assemble app - manifest filter task removes forbidden schemes`(@TempDir projectDir: File) {
        generateTestProject(projectDir)

        gradlew(projectDir, ":app:assembleRelease").assertThat().buildSuccessful()

        validateManifest(
            isForLib = false,
            withFilter = true,
            manifestProjectDir = File(projectDir, "app"),
            manifestValidator = { manifest ->
                manifest.assertContainsActivity("com.avito.deeplink_generator.SomeActivity")
                manifest.assertContainsIntentFilter()
                manifest.assertContainsDeeplink(deeplink = Deeplink("ru.avito", "1", "/feed"), dataPosition = 1)
                manifest.assertContainsDeeplink(deeplink = Deeplink("com.scheme1", "1", "/feed"), dataPosition = 2)
                manifest.assertContainsDeeplink(deeplink = Deeplink("ru.avito", "1", "/profile"), dataPosition = 3)
                manifest.assertDeeplinkCount(count = 3)
            }
        )
    }

    private fun generateTestProject(projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.deeplink-manifest-filter")
                    },
                    dependencies = setOf(
                        project(":lib"),
                    ),
                    useKts = true,
                    buildGradleExtra = """
                        deeplinkManifestFilter {
                            variantToForbiddenSchemes.put("release", setOf("com.scheme2"))
                        }
                    """.trimIndent(),
                ),
                libModule(
                    buildScriptLinks = listOf(
                        "1/feed",
                        "1/profile"
                    ),
                    codeLinks = listOf(
                        "1/feed ru.avito,com.scheme1,com.scheme2",
                        "1/profile ru.avito,com.scheme2"
                    )
                )
            ),
            gradleProperties = mapOf(
                "avito.deeplinks.filter.enabled" to "true",
            )
        ).generateIn(projectDir)
    }
}
