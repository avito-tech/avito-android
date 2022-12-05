package com.avito.android.proguard_guard

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UpdateLockedConfigurationTest {

    @field:TempDir
    private lateinit var projectDir: File

    @Test
    fun `replace existing file - success`() {
        val mergedConfigPath = "merged.pro"
        val mergedConfigContent = """
            #comment
            -allowaccessmodification
            """.trimIndent()
        val lockedConfigPath = "guarded-configuration.pro"
        val lockedConfigContent = "-optimizationpasses 5"
        val configFilesCreator: File.(AndroidAppModule) -> Unit = {
            file(name = mergedConfigPath, content = mergedConfigContent)
            file(name = lockedConfigPath, content = lockedConfigContent)
        }
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.proguard-guard")
                    },
                    buildGradleExtra = """
                        android.buildTypes.debug.minifyEnabled = true
                        proguardGuard {
                            lockVariant("debug", file("$mergedConfigPath")) {
                                lockedConfigurationFile = file("$lockedConfigPath")
                            }
                        }
                        """.trimIndent(),
                    mutator = configFilesCreator,
                )
            ),
        ).generateIn(projectDir)

        val build = gradlew(projectDir, "updateDebugLockedProguard")
        val locked = projectDir.resolve("app/$lockedConfigPath")

        build.assertThat().buildSuccessful()
        assertThat(locked.readText()).isEqualTo(mergedConfigContent)
    }
}
