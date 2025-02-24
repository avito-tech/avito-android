package com.avito.i18n.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class TranslationFileTaskTest {
    @Test
    fun `run update translations task without strings file - failed`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.i18n")
                    },
                    enableKotlinAndroidPlugin = false
                )
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir = projectDir,
            ":app:${TranslationPlugin.CREATE_COMPONENT_TASK_NAME}",
            expectFailure = true
        ).assertThat()
            .buildFailed()
    }

    @Test
    fun `run update translations task with strings file - successful`(@TempDir projectDir: File) {
        val moduleName = "app"
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = moduleName,
                    plugins = plugins {
                        id("com.avito.android.i18n")
                    },
                    buildGradleExtra = """
                        translation {
                            locales = ["en", "en-US"]
                        }
                    """.trimIndent(),
                    enableKotlinAndroidPlugin = false
                )
            )
        ).generateIn(projectDir)

        val localeIsEmpty = ""

        projectDir.module(moduleName) {
            dir(TranslationPlugin.RES_PATH) {
                file(
                    name = localeIsEmpty.getStringsFile(),
                    content = ORIGINAL_FILE_CONTENT
                )
            }
        }

        gradlew(projectDir, ":app:${TranslationPlugin.TRANSLATION_TASK_NAME}")
            .assertThat()
            .buildSuccessful()

        val enUSFile = File(projectDir, "app/${TranslationPlugin.RES_PATH}values-en-rUS/strings.xml")
        assertThat(enUSFile.exists())
            .isTrue()
        assertThat(enUSFile.readText())
            .contains(TRANSLATED_FILE_CONTENT)

        val enFile = File(projectDir, "app/${TranslationPlugin.RES_PATH}values-en/strings.xml")

        assertThat(enFile.exists())
            .isTrue()
        assertThat(enFile.readText())
            .contains(TRANSLATED_FILE_CONTENT)
    }
}
