package com.avito.i18n.plugin

import com.avito.android.tls.test.createMtlsExtensionString
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TranslationPluginTest {

    @Test
    fun `configuration with applied plugin to root failed`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.i18n")
            }
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            "help",
            expectFailure = true
        ).assertThat()
            .buildFailed()
    }

    @Test
    fun `configuration with applied plugin to subproject successful`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.tls-configuration")
            },
            buildGradleExtra = """
                ${createMtlsExtensionString()}
            """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.i18n")
                    },
                    enableKotlinAndroidPlugin = false
                )
            ),
            useKts = true
        ).generateIn(projectDir)

        gradlew(projectDir, ":app:help")
            .assertThat()
            .buildSuccessful()
    }
}
