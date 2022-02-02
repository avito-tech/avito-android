package com.avito.emcee

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class EmceePluginTest {

    @Test
    fun `configuration - passes - without emcee extension configured`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.emcee")
                    }
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "help").assertThat().buildSuccessful()
    }

    @Test
    fun `configuration - all args passed correctly`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = plugins {
                        id("com.avito.android.emcee")
                    },
                    useKts = true,
                    imports = listOf("import java.time.Duration"),
                    buildGradleExtra = """
                    |emcee {
                    |   job.id.set("AvitoComponentTests#PR-2214")
                    |   job.groupId.set("PRTests#PR-2214")
                    |   job.priority.set(100)
                    |   job.groupPriority.set(100)
                    |   retries.set(2)
                    |   deviceApis.add(22)
                    |   deviceApis.add(30)
                    |   testTimeout.set(Duration.ofSeconds(120))
                    |   queueBaseUrl.set("http://emcee.queue")
                    |}
                    |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "emceeTestDebug").assertThat().buildSuccessful()
    }
}
