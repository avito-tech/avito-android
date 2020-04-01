package com.avito.instrumentation.impact

import com.avito.bytecode.metadata.ModulePath
import com.avito.instrumentation.impact.util.generateProjectWithScreensInMultiModule
import com.avito.instrumentation.impact.util.generateProjectWithScreensInSingleModule
import com.avito.instrumentation.impact.util.impactAnalysisScreenIdsOutput
import com.avito.instrumentation.impact.util.kotlinModuleTestDependency
import com.avito.instrumentation.impact.util.projectToChange
import com.avito.test.gradle.ciRun
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.nio.file.Paths

class InstrumentationGettingScreenIdIntegrationTest {

    private val targetBranch = "develop"

    @Test
    fun `screens ids file contains screen from local androidTest module`(@TempDir tempPath: Path) {
        val testProjectDir = tempPath.toFile()
        val outputDir = Paths.get(
            "$testProjectDir",
            projectToChange,
            "build",
            "outputs",
            "reports",
            "impact-analysis"
        ).toFile()

        val sourceBranch = "new-test-in-$projectToChange"

        generateProjectWithScreensInSingleModule(testProjectDir, outputDir)

        ciRun(
            testProjectDir,
            ":$projectToChange:analyzeTestBytecode",
            "--rerun-tasks",
            "--no-build-cache",
            "-PgitBranch=$sourceBranch",
            "-PtargetBranch=$targetBranch"
        ).also { it.assertThat().buildSuccessful() }

        val output = impactAnalysisScreenIdsOutput(outputDir)

        assertWithMessage("output contains screen from local androidTest module")
            .that(output)
            .containsEntry("test.SomeScreen", ModulePath(":$projectToChange"))
    }

    @Test
    fun `screens ids file contains screen from dependant kotlin module`(@TempDir tempPath: Path) {
        val testProjectDir = tempPath.toFile()
        val outputDir = Paths.get(
            "$testProjectDir",
            projectToChange,
            "build",
            "outputs",
            "reports",
            "impact-analysis"
        ).toFile()

        val sourceBranch = "new-test-in-$projectToChange"

        generateProjectWithScreensInMultiModule(testProjectDir, outputDir)

        ciRun(
            testProjectDir,
            ":$projectToChange:analyzeTestBytecode",
            "--rerun-tasks",
            "--no-build-cache",
            "-PgitBranch=$sourceBranch",
            "-PtargetBranch=$targetBranch"
        ).also { it.assertThat().buildSuccessful() }

        val output = impactAnalysisScreenIdsOutput(outputDir)

        assertWithMessage("output contains screen from dependant kotlin module")
            .that(output.entries.map { it.toPair() })
            .containsExactlyElementsIn(
                listOf(
                    "kotlinModule.marker.ScreenFromKotlinModule" to ModulePath(":$kotlinModuleTestDependency"),
                    "kotlinModule.marker.Screen2FromKotlinModule" to ModulePath(":$kotlinModuleTestDependency")
                )
            )
    }
}
