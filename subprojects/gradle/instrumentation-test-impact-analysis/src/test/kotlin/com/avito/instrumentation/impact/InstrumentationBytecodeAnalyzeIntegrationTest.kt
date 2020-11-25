package com.avito.instrumentation.impact

import com.avito.instrumentation.impact.util.generateProjectWithScreensInMultiModule
import com.avito.instrumentation.impact.util.generateProjectWithScreensInSingleModule
import com.avito.instrumentation.impact.util.impactAnalysisScreensToTestsOutput
import com.avito.instrumentation.impact.util.projectToChange
import com.avito.test.gradle.ciRun
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

class InstrumentationBytecodeAnalyzeIntegrationTest {

    private val targetBranch = "develop"

    @Test
    fun `screens to tests mapping file contains tests connected with screen in single module project`(
        @TempDir testProjectDir: File
    ) {
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

        val output = impactAnalysisScreensToTestsOutput(outputDir)

        assertWithMessage("impact analysis result has test.SomeScreen")
            .that(output)
            .containsKey("test.SomeScreen")

        assertWithMessage("test.SomeScreen affects tests")
            .that(output["test.SomeScreen"])
            .containsExactlyElementsIn(
                setOf(
                    "test.OldTestClass.testUsedSomeScreenThroughAbstraction",
                    "test.OldTestClass.testUsedSomeScreenThroughLambdaAndAbstraction",
                    "test.OldTestClass.testUsedSomeScreenDirectly"
                )
            )
        assertWithMessage("test.OldTestClass.testUsedNothing has moved to fallback")
            .that(output)
            .containsEntry("*", setOf("test.OldTestClass.testUsedNothing"))
    }

    @Test
    fun `screens to tests mapping file contains tests connected with screen in multi module project`(
        @TempDir testProjectDir: File
    ) {
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

        val output = impactAnalysisScreensToTestsOutput(outputDir)

        assertWithMessage("impact analysis result has test.SomeScreen")
            .that(output)
            .containsKey("kotlinModule.marker.ScreenFromKotlinModule")

        assertWithMessage("kotlinModule.marker.ScreenFromKotlinModule affects tests")
            .that(output["kotlinModule.marker.ScreenFromKotlinModule"])
            .containsExactlyElementsIn(
                setOf(
                    "test.TestClass.testUsedSomeScreenThroughAbstraction",
                    "test.TestClass.testUsedSomeScreenThroughLambdaAndAbstraction",
                    "test.TestClass.testUsedBothScreensThroughImplementationOfBaseClassThatUsesScreen",
                    "test.TestClass.testUsedSomeScreenDirectly"
                )
            )
        assertWithMessage("kotlinModule.marker.Screen2FromKotlinModule affects tests")
            .that(output["kotlinModule.marker.Screen2FromKotlinModule"])
            .containsExactlyElementsIn(
                setOf(
                    "test.TestClass.testUsedBothScreensThroughImplementationOfBaseClassThatUsesScreen"
                )
            )

        assertWithMessage("test.TestClass.testUsedNothing has moved to fallback")
            .that(output)
            .containsEntry("*", setOf("test.TestClass.testUsedNothing"))
    }
}
