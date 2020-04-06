package com.avito.instrumentation.impact

import com.avito.instrumentation.impact.util.generateProjectWithScreensInSingleModule
import com.avito.instrumentation.impact.util.impactAnalysisTestsChangedOutput
import com.avito.instrumentation.impact.util.impactAnalysisTestsToRunOutput
import com.avito.instrumentation.impact.util.projectToChange
import com.avito.test.gradle.TestProjectGenerator.Companion.sharedModule
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.commit
import com.avito.test.gradle.dir
import com.avito.test.gradle.git
import com.avito.test.gradle.kotlinClass
import com.google.common.truth.Truth.assertWithMessage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class InstrumentationAnalyzeImpactAnalysisIntegrationTest {

    private val targetBranch = "develop"

    @Test
    fun `new instrumentation tests in module without marker - run impact analysis`(@TempDir testProjectDir: File) {
        val outputDir = Paths.get(
            "$testProjectDir",
            projectToChange,
            "build",
            "outputs",
            "reports",
            "impact-analysis"
        ).toFile()

        val sourceBranch = "new-test-in-$projectToChange"

        with(generateProjectWithScreensInSingleModule(testProjectDir, outputDir)) {
            git("checkout -b $sourceBranch $targetBranch")

            dir("$projectToChange/src/androidTest/kotlin/test") {
                kotlinClass("NewTestClass") {
                    """
                        package test

                        import org.junit.Test

                        class NewTestClass {

                            @Test
                            fun newAddedTest() { }
                        }
                        """.trimIndent()
                }
            }

            commit()
        }

        analyzeTestImpact(
            testProjectDir,
            sourceBranch
        )

        assertWithMessage("added test in tests to run output")
            .that(impactAnalysisTestsToRunOutput(outputDir))
            .contains("test.NewTestClass.newAddedTest")

        assertWithMessage("added test in changed tests output")
            .that(impactAnalysisTestsChangedOutput(outputDir))
            .contains("test.NewTestClass.newAddedTest")
    }

    @Test
    fun `feature module(not linked to screen) changed`(@TempDir testProjectDir: File) {
        val outputDir = Paths.get(
            "$testProjectDir",
            projectToChange,
            "build",
            "outputs",
            "reports",
            "impact-analysis"
        ).toFile()

        val sourceBranch = "changes-in-$sharedModule"

        with(generateProjectWithScreensInSingleModule(testProjectDir, outputDir)) {
            git("checkout -b $sourceBranch $targetBranch")

            dir("$sharedModule/src/main/kotlin/com/newfeature")
            kotlinClass("NewFeature")

            commit()
        }

        analyzeTestImpact(
            testProjectDir,
            sourceBranch
        )

        assertWithMessage("test that affects that screen should appear in output artifact")
            .that(impactAnalysisTestsToRunOutput(outputDir))
            .containsExactlyElementsIn(
                listOf(
                    "test.OldTestClass.testUsedSomeScreenDirectly",
                    "test.OldTestClass.testUsedSomeScreenThroughAbstraction",
                    "test.OldTestClass.testUsedNothing",
                    "test.OldTestClass.testUsedSomeScreenThroughLambdaAndAbstraction"
                )
            )
    }

    private fun analyzeTestImpact(dir: File, sourceBranch: String) {
        val result = ciRun(
            dir,
            ":$projectToChange:analyzeTestImpact",
            "--stacktrace",
            "-Pavito.stats.enabled=false",
            "-PgitBranch=$sourceBranch",
            "-PtargetBranch=$targetBranch",
            "-Pavito.stats.enabled=false",
            "-Pavito.stats.host=http://stats",
            "-Pavito.stats.fallbackHost=http://stats",
            "-Pavito.stats.port=80",
            "-Pavito.stats.namespace=android"
        )
        result.assertThat().buildSuccessful()
    }
}
