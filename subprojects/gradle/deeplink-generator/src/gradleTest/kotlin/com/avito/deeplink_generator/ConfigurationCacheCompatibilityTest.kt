package com.avito.deeplink_generator

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    dependencies = setOf(
                        project(":feed"),
                    ),
                ),
                AndroidLibModule(
                    name = "feed",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.deeplink-generator")
                    },
                    buildGradleExtra = """
                        deeplinkGenerator {
                            activityIntentFilterClass.set("com.avito.deeplink_generator.SomeActivity")
                            defaultScheme.set("ru.avito")
                           
                            publicDeeplinks(
                                "1/feed"
                            ) 
                        }
                    """.trimIndent()

                )
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(projectDir: File): TestResult {
        return gradlew(
            projectDir,
            ":app:assembleRelease",
            dryRun = true,
            configurationCache = true
        )
    }
}
