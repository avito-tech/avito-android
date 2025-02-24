package com.avito.i18n.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class CreateComponentTaskTest {
    @Test
    fun `create component successful`(@TempDir projectDir: File) {
        val moduleName = "app"
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = moduleName,
                    plugins = plugins {
                        id("com.avito.android.i18n")
                    },
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

        gradlew(projectDir, ":app:createTranslationComponent")
            .assertThat()
            .buildSuccessful()
    }
}
