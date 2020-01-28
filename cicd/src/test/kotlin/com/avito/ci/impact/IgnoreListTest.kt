package com.avito.ci.impact

import com.avito.ci.assertMarkedModules
import com.avito.ci.detectChangedModules
import com.avito.ci.generateProjectWithImpactAnalysis
import com.avito.test.gradle.commit
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class IgnoreListTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()

        generateProjectWithImpactAnalysis(projectDir)

        with(projectDir) {

            file(
                ".tia_ignore", """
                    *.md
                    ignored_directory/*
                """.trimIndent()
            )
            commit()
            git("checkout -b develop")
        }
    }

    @Test
    fun `changes in tracked and TIA ignored files - detects no changes`() {
        val sourceBranch = "ignored-changes develop"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch")
            file("README.md")
            dir("ignored_directory") {
                file("KotlinClass.kt")
            }
            dir("app") {
                file("README.md")
            }
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
            implementation = emptySet(),
            unitTests = emptySet(),
            androidTests = emptySet()
        )
    }
}
