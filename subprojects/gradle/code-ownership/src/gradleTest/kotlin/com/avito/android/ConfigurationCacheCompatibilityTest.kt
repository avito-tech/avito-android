package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationCacheCompatibilityTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            modules = listOf(
                AndroidLibModule(
                    name = "lib",
                    imports = listOf("import com.avito.android.model.Owner"),
                    buildGradleExtra = """
                        |object Speed : Owner { }
                        |
                        |ownership {
                        |    owners(Speed)
                        |}
                    """.trimMargin(),
                    useKts = true
                )
            )
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        runTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runTask(tempDir: File): TestResult {
        return gradlew(
            tempDir,
            "exportCodeOwnershipInfo",
            "-Pavito.ownership.strictOwnership=true",
            dryRun = true,
            configurationCache = true
        )
    }
}
