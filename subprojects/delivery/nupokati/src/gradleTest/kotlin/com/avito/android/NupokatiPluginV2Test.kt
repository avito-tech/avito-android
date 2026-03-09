package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class NupokatiPluginV2Test {

    @Test
    fun `configuration successful - without nupokati config provided`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.qapps")
                        id("com.avito.android.nupokati")
                    }
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "tasks").assertThat().buildSuccessful()
    }

    @Test
    fun `debug task is created - when debug is set as releaseVariant`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.qapps")
                        id("com.avito.android.nupokati")
                    },
                    buildGradleExtra = """
                            |nupokati {
                            |    v2("releaseV2") {
                            |        releaseVariant.set("debug")
                            |    }
                            |}
                            |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "tasks").assertThat()
            .buildSuccessful()
            .outputContains("artifactoryBackupDebug")
    }
}
