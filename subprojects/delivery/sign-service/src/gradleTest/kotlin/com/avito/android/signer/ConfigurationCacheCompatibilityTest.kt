package com.avito.android.signer

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    private val applicationId = "com.app"

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    packageName = applicationId,
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id(PLUGIN_ID)
                    },
                    useKts = true,
                    buildGradleExtra = """
                        signer {
                            serviceUrl.set("http://stub")
                            apkSignTokens.put("$applicationId", "12345")
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
            ":app:signApkViaServiceRelease",
            dryRun = true,
            configurationCache = true
        )
    }
}
