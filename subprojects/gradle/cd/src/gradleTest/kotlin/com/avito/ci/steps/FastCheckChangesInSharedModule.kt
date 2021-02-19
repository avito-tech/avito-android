package com.avito.ci.steps

import com.avito.ci.assertAffectedModules
import com.avito.ci.generateProjectWithImpactAnalysis
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.commit
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.mutate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class FastCheckChangesInSharedModule {

    private lateinit var projectDir: File

    private val targetBranch = "develop"
    private val sourceBranch = "changes-in-shared"

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()

        generateProjectWithImpactAnalysis(projectDir)
    }

    @Test
    fun `fastCheck runs all applications build tasks - changed implementation in dependent module`() {
        mutateFile("${TestProjectGenerator.sharedModule}/src/main/kotlin/SomeClass.kt")

        val result = runTask("fastCheck")

        result.assertAffectedModules(
            "packageDebug",
            expectedModules = setOf(":${TestProjectGenerator.appA}", ":${TestProjectGenerator.appB}")
        )
    }

    @Test
    fun `fastCheck runs only unit tests - changed unit test in dependent module`() {
        mutateFile("${TestProjectGenerator.sharedModule}/src/test/kotlin/SomeClass.kt")

        val result = runTask("fastCheck")

        result.assertAffectedModules(
            "packageDebug",
            expectedModules = emptySet()
        )
        result.assertAffectedModules(
            "test",
            expectedModules = setOf(":${TestProjectGenerator.sharedModule}")
        )
    }

    private fun mutateFile(path: String) {
        with(projectDir) {
            git("checkout -b $targetBranch")

            git("checkout -b $sourceBranch $targetBranch")
            file(path).mutate()
            commit()
        }
    }

    private fun runTask(taskName: String): TestResult =
        gradlew(
            projectDir,
            taskName,
            "-Pci=true",
            "-PgitBranch=$sourceBranch",
            "-PtargetBranch=$targetBranch",
            dryRun = true
        )
}
