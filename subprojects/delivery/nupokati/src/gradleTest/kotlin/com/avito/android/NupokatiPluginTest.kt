package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class NupokatiPluginTest {

    @Test
    fun `configuration successful - without nupokati config provided`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.nupokati")
                    }
                )
            )
        ).generateIn(projectDir)

        gradlew(projectDir, "tasks").assertThat().buildSuccessful()
    }
}
