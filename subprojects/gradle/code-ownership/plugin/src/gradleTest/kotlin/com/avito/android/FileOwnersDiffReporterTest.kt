package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class FileOwnersDiffReporterTest {

    @Test
    fun `generate code ownership file report report - file created and filled with data`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            imports = listOf(
                "import com.avito.android.model.AvitoCodeOwner",
                "import com.avito.android.diff.provider.OwnersProvider",
                "import com.avito.android.diff.report.OwnersDiffReportDestination",
                "import com.avito.android.network.FakeAvitoOwnersClient",
                "import com.avito.android.network.FakeAlertinoSender",
                "import com.avito.android.diff.formatter.DefaultOwnersDiffMessageFormatter",
                "import com.avito.android.model.Unit"
            ),
            buildGradleExtra = """
                object Speed : AvitoCodeOwner {
                    override val type = Unit("Speed", "1")
                    override fun toString() = "Speed"
                }
                object MobileArchitecture : AvitoCodeOwner {
                    override val type = Unit("Mobile Architecture", "2")
                    override fun toString() = "Mobile Architecture"
                }
                object Performance : AvitoCodeOwner {
                    override val type = Unit("Performance", "3")
                    override fun toString() = "Performance"
                }
                
                ownership {
                    avitoOwnersClient.set(FakeAvitoOwnersClient())
                    alertinoSender.set(FakeAlertinoSender())
                }
                
                codeOwnershipDiffReport { 
                    messageFormatter.set(DefaultOwnersDiffMessageFormatter())
                    expectedOwnersProvider.set(OwnersProvider { setOf(Speed) })
                    actualOwnersProvider.set(OwnersProvider { setOf(Performance, MobileArchitecture) })
                    diffReportDestination.set(OwnersDiffReportDestination.File(project.projectDir)) 
                } 
            """.trimIndent(),
            useKts = true,
            modules = listOf(AndroidLibModule(name = "lib"))
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        val file = File(projectDir, "ownership_diff_report.txt")
        Truth
            .assertThat(file.exists())
            .isTrue()

        Truth
            .assertThat(file.readText())
            .isEqualTo(
                """
            |Found difference in code owners structure!
            |*Removed owners:* Speed
            |*Added owners:* Performance, Mobile Architecture
            """.trimMargin()
            )
    }

    private fun runTask(tempDir: File): TestResult {
        return gradlew(
            tempDir,
            "reportCodeOwnershipDiff",
            useTestFixturesClasspath = true
        )
    }
}
