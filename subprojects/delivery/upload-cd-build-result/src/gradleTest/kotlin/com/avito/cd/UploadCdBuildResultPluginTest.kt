package com.avito.cd

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class UploadCdBuildResultPluginTest {

    @Test
    fun `configuration - success`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id(PLUGIN_ID)
                    },
                    enableKotlinAndroidPlugin = false,
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "tasks", dryRun = false)
    }
}
