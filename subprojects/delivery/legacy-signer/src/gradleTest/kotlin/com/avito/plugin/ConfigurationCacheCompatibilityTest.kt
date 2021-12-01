package com.avito.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    /**
     * todo onlyIf { } not supported, enabled after migration to new tasks
     */
    @Disabled
    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.signer")
                    },
                    useKts = true,
                    buildGradleExtra = """
                        signService {
                            url.set("http://stub")
                            
                            apk("release", "12345")
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
