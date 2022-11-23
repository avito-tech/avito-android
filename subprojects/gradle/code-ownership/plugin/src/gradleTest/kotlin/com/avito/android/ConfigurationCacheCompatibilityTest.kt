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
    fun `configuration with applied plugin and info report - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            modules = listOf(
                AndroidLibModule(
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                    },
                    name = "lib",
                    imports = listOf(
                        "import com.avito.android.model.Owner",
                        "import com.avito.android.OwnerSerializer"
                    ),
                    buildGradleExtra = """
                        |object Speed : Owner { }
                        |object SpeedOwnerSerializer : OwnerSerializer {
                        |   override fun deserialize(rawOwner: String): com.avito.android.model.Owner {
                        |       return Speed
                        |   }
                        |   
                        |   override fun serialize(owner: Owner): String {
                        |       return "Speed"
                        |   }
                        |} 
                        |
                        |ownership {
                        |    owners(Speed)
                        |    ownerSerializer.set(SpeedOwnerSerializer)
                        |}
                    """.trimMargin(),
                    useKts = true
                )
            )
        ).generateIn(projectDir)

        runInfoReportTask(projectDir).assertThat().buildSuccessful()

        runInfoReportTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    @Test
    fun `configuration with applied plugin and file report - ok`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            imports = listOf(
                "import com.avito.android.model.StubOwner",
                "import com.avito.android.diff.provider.SimpleOwnersProvider",
                "import com.avito.android.diff.report.OwnersDiffReportDestination"
            ),
            buildGradleExtra = """
                codeOwnershipDiffReport { 
                    expectedOwnersProvider.set(SimpleOwnersProvider(setOf()))
                    actualOwnersProvider.set(SimpleOwnersProvider(setOf(StubOwner)))
                    diffReportDestination.set(OwnersDiffReportDestination.File(project.projectDir))
                } 
            """.trimIndent(),
            useKts = true,
            modules = listOf(AndroidLibModule(name = "lib"))
        ).generateIn(projectDir)

        runDiffReportTask(projectDir).assertThat().buildSuccessful()

        runDiffReportTask(projectDir).assertThat().buildSuccessful().configurationCachedReused()
    }

    private fun runDiffReportTask(tempDir: File): TestResult {
        return gradlew(
            tempDir,
            "reportCodeOwnershipDiff",
            dryRun = false,
            configurationCache = true,
            useTestFixturesClasspath = true
        )
    }

    private fun runInfoReportTask(tempDir: File): TestResult {
        return gradlew(
            tempDir,
            "exportInternalDepsCodeOwners",
            "-Pavito.ownership.strictOwnership=true",
            dryRun = true,
            configurationCache = true
        )
    }
}
