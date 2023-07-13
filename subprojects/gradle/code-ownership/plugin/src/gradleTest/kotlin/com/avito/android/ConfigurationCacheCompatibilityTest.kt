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
                        "import com.avito.android.OwnerIdSerializer",
                        "import com.avito.android.OwnerNameSerializer",
                        "import com.avito.android.OwnerSerializerProvider",
                    ),
                    buildGradleExtra = """
                        |object Speed : Owner { }
                        |object SpeedOwnerSerializersProvider : OwnerSerializerProvider {
                        |
                        |   override fun provideIdSerializer() = object : OwnerIdSerializer {
                        |       override fun deserialize(ownerName: String): com.avito.android.model.Owner {
                        |           return Speed
                        |       }
                        |       
                        |       override fun serialize(owner: Owner): List<String> {
                        |           return listOf("Speed")
                        |       }
                        |   }
                        |   
                        |   override fun provideNameSerializer() = object : OwnerNameSerializer { 
                        |       override fun deserialize(ownerId: String): com.avito.android.model.Owner {
                        |           return Speed
                        |       }
                        |       
                        |       override fun serialize(owner: Owner): String {
                        |           return "Speed"
                        |       }
                        |   }
                        |} 
                        |
                        |ownership {
                        |    owners(Speed)
                        |    ownerSerializersProvider.set(SpeedOwnerSerializersProvider)
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
