package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.Assert
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
                id("com.avito.android.code-ownership-report")
            },
            imports = listOf(
                "import com.avito.android.model.Owner",
                "import com.avito.android.diff.extractor.OwnersExtractor",
                "import com.avito.android.diff.report.OwnersDiffReportDestination"
            ),
            buildGradleExtra = """
                object Speed : Owner { override fun toString() = "Speed" }
                object MobileArchitecture : Owner { override fun toString() = "Mobile Architecture" }
                object Performance : Owner { override fun toString() = "Performance" }
                
                codeOwnershipDiffReport { 
                    expectedOwnersExtractor.set(OwnersExtractor { setOf(Speed) })
                    actualOwnersExtractor.set(OwnersExtractor { setOf(Performance, MobileArchitecture) })
                    diffReportDestination.set(OwnersDiffReportDestination.File(project.projectDir)) 
                } 
            """.trimIndent(),
            useKts = true,
            modules = listOf(AndroidLibModule(name = "lib"))
        ).generateIn(projectDir)

        runTask(projectDir).assertThat().buildSuccessful()

        val file = File(projectDir, "ownership_diff_report.txt")
        Assert.assertTrue(file.exists())

        Assert.assertEquals(
            """
            |Found difference in code owners structure!
            |*Removed owners:* Speed
            |*Added owners:* Performance, Mobile Architecture
            """.trimMargin(),
            file.readText()
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
