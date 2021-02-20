package com.avito.ci.steps

import com.avito.ci.assertAffectedModules
import com.avito.ci.generateProjectWithImpactAnalysis
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.ciRun
import com.avito.test.gradle.git
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class FastCheckNoChanges {

    private lateinit var projectDir: File

    private val targetBranch = "develop"
    private val sourceBranch = "no-changes"

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
    fun `fastCheck - does not trigger any of assembleDebug tasks - thre is no changes`() {
        val result = runTask("fastCheck")

        result.assertAffectedModules("assemble", emptySet())
    }

    private fun runTask(taskName: String): TestResult =
        ciRun(projectDir, taskName, "-PgitBranch=$sourceBranch", "-PtargetBranch=$targetBranch", dryRun = true)
}
