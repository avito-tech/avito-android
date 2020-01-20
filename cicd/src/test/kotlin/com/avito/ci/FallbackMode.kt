package com.avito.ci

import com.avito.test.gradle.TestProjectGenerator.Companion.allModules
import com.avito.test.gradle.commit
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class FallbackMode {

    private lateinit var projectDir: File

    private val targetBranch = "develop"
    private val sourceBranch = "changes-out-of-all-modules"

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()

        generateProjectWithImpactAnalysis(projectDir)

        with(projectDir) {
            git("checkout -b $targetBranch")
        }
    }

    @Test
    fun `changes out of all modules - switches to fallback mode and test all modules`() {
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
            file("unknown-new.properties")
            commit()
        }
        val result = detectChangedModules(
            projectDir,
            "-Pci=true",
            "-PgitBranch=$sourceBranch",
            "-PtargetBranch=$targetBranch"
        )

        result.assertMarkedModules(
            projectDir,
            implementation = allModules,
            unitTests = allModules,
            androidTests = allModules
        )
    }
}
