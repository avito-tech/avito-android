package com.avito.ci.steps

import com.avito.ci.generateProjectWithImpactAnalysis
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.commit
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.avito.test.gradle.mutate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

internal class UnitTestCheckTest {

    private lateinit var projectDir: File

    private val targetBranch = "develop"
    private val sourceBranch = "changes"

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()

        generateProjectWithImpactAnalysis(projectDir)

        with(projectDir) {
            git("checkout -b $targetBranch")
            git("checkout -b $sourceBranch $targetBranch")
        }
    }

    @Test
    fun `no changes - unit tests isn't executed `() {
        val result = runTaskFastCheck()
        result.assertThat()
            .tasksShouldNotBeTriggered(
                ":appA:test",
                ":appB:test",
                ":shared:test",
                ":transitive:test",
                ":independent:test"
            )
    }

    @Test
    fun `android test is changed - unit tests isn't executed`() {
        with(projectDir) {
            file("${TestProjectGenerator.appA}/src/androidTest/kotlin/com/appA/SomeClass.kt").mutate()
            commit()
        }
        val result = runTaskFastCheck()
        result.assertThat()
            .tasksShouldNotBeTriggered(
                ":appA:test",
                ":appB:test",
                ":shared:test",
                ":transitive:test",
                ":independent:test"
            )
    }

    @Test
    fun `implementation is changed - unit tests is executed`() {
        with(projectDir) {
            file("${TestProjectGenerator.appA}/src/main/kotlin/com/appA/SomeClass.kt").mutate()
            commit()
        }
        val result = runTaskFastCheck()
        result.assertThat()
            .tasksShouldBeTriggered(":appA:test")

        result.assertThat()
            .tasksShouldNotBeTriggered(
                ":appB:test",
                ":shared:test",
                ":transitive:test",
                ":independent:test"
            )
    }

    @Test
    fun `unit test is changed - unit tests is executed`() {
        with(projectDir) {
            file("${TestProjectGenerator.appA}/src/test/kotlin/com/appA/SomeClass.kt").mutate()
            commit()
        }
        val result = runTaskFastCheck()

        result.assertThat()
            .tasksShouldBeTriggered(":appA:test")

        result.assertThat()
            .tasksShouldNotBeTriggered(
                ":appB:test",
                ":shared:test",
                ":transitive:test",
                ":independent:test"
            )
    }

    private fun runTaskFastCheck(): TestResult =
        ciRun(projectDir, "fastCheck", "-PgitBranch=$sourceBranch", "-PtargetBranch=$targetBranch", dryRun = true)
}
