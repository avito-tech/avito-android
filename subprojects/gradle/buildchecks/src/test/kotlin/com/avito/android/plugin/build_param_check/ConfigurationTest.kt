package com.avito.android.plugin.build_param_check

import com.avito.test.gradle.KotlinModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationTest {

    @Test
    fun `empty legacy configuration`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.buildchecks"),
            modules = listOf(
                KotlinModule(
                    name = "someModule",
                    plugins = listOf("com.avito.android.module-types")
                )
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            "help",
            "-P$legacyEnabledGradleProperty=false",
            "-P$androidJarRevision=1",
            //todo make params optional
            "-Pavito.stats.host=localhost",
            "-Pavito.stats.fallbackHost=localhost",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=stub"
        )
            .assertThat()
            .buildSuccessful()
    }

    @Test
    fun `empty dsl`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.buildchecks"),
            modules = listOf(
                KotlinModule(
                    name = "someModule",
                    plugins = listOf("com.avito.android.module-types")
                )
            ),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                }
            """.trimIndent()
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            "help",
            //todo make params optional
            "-Pavito.stats.host=localhost",
            "-Pavito.stats.fallbackHost=localhost",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=stub"
        )
            .assertThat()
            .buildSuccessful()
    }
}
