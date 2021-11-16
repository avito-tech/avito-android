package com.avito.ci.steps

import com.avito.ci.assertAffectedModules
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

class FastCheckOneAppBChanges {

    private lateinit var projectDir: File

    private val targetBranch = "develop"
    private val sourceBranch = "changes-in-the-${TestProjectGenerator.appB}"

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()

        generateProjectWithImpactAnalysis(projectDir)

        with(projectDir) {
            git("checkout -b $targetBranch")

            git("checkout -b $sourceBranch $targetBranch")
            file("${TestProjectGenerator.appB}/src/main/kotlin/SomeClass.kt").mutate()
            commit()
        }
    }

    @Test
    fun `fastCheck triggers build only in appA`() {
        val result = runTask("fastCheck")

        result.assertAffectedModules("packageDebug", setOf(":${TestProjectGenerator.appB}"))
    }

    private fun runTask(taskName: String): TestResult =
        ciRun(projectDir, taskName, "-PgitBranch=$sourceBranch", "-PtargetBranch=$targetBranch", dryRun = true)
}
