package com.avito.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    /**
     * TODO
     *
     * 138 problems were found storing the configuration cache, 4 of which seem unique.
     *
     * Task `:app:signApkViaServiceRelease` of type `com.avito.plugin.SignApkTask`:
     * cannot serialize object of type 'org.gradle.api.internal.artifacts.configurations.DefaultConfiguration',
     * a subtype of 'org.gradle.api.artifacts.Configuration',
     * as these are not supported with the configuration cache.
     * See https://docs.gradle.org/6.8/userguide/configuration_cache.html#config_cache:requirements:disallowed_types
     *
     * Task `:app:signApkViaServiceRelease` of type `com.avito.plugin.SignApkTask`:
     * cannot serialize object of type 'org.gradle.api.internal.project.DefaultProject',
     * a subtype of 'org.gradle.api.Project', as these are not supported with the configuration cache.
     * 1See https://docs.gradle.org/6.8/userguide/configuration_cache.html#config_cache:requirements:disallowed_types
     *
     * Task `:app:signApkViaServiceRelease` of type `com.avito.plugin.SignApkTask`: cannot serialize
     * object of type 'org.gradle.api.internal.artifacts.configurations.DefaultConfigurationContainer',
     * a subtype of 'org.gradle.api.artifacts.ConfigurationContainer',
     * as these are not supported with the configuration cache.
     * See https://docs.gradle.org/6.8/userguide/configuration_cache.html#config_cache:requirements:disallowed_types
     *
     * Task `:app:signApkViaServiceRelease` of type `com.avito.plugin.SignApkTask`:
     * value 'flatmap(provider(class com.android.build.gradle.internal.SdkComponentsBuildService))'
     * failed to unpack provider
     */
    @Disabled
    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = listOf("com.avito.android.signer"),
                    buildGradleExtra = """
                        signService {
                            host = "http://stub"
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
