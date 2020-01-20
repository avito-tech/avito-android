package com.avito.ci

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.EmptyModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.commit
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.avito.test.gradle.mutate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class TestDependencies {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()

        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    "app", dependencies = """
                        implementation project(':feature')
                    """.trimIndent()
                ),
                AndroidLibModule(
                    "feature", dependencies = """
                        implementation project(':utils')
                        testImplementation project(':test:unit')
                        androidTestImplementation project(':test:ui')
                    """.trimIndent()
                ),
                AndroidLibModule("utils"),
                EmptyModule(
                    "test", modules = listOf(
                    AndroidLibModule("unit"),
                    AndroidLibModule("ui")
                )
                )
            )
        ).generateIn(projectDir)

        with(projectDir) {
            git("checkout -b develop")
        }
    }

    @Test
    fun `no changes at all - doesn't detect changed modules`() {
        val sourceBranch = "no-changes"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
        }

        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = emptySet(),
            unitTests = emptySet(),
            androidTests = emptySet()
        )
    }

    @Test
    fun `change in the implementation dependency - detects changes by implementation dependencies`() {
        val sourceBranch = "changes-in-the-implementation-dep"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
            file("utils/src/main/kotlin/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = setOf(":utils", ":feature", ":app"),
            unitTests = setOf(":utils", ":feature", ":app"),
            androidTests = setOf(":utils", ":feature", ":app")
        )
    }

    @Test
    fun `change in the unit test dependency - detects changes by test dependencies`() {
        val sourceBranch = "changes-in-the-test-dependant-module"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
            file("test/unit/src/main/kotlin/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = setOf(":test:unit"),
            unitTests = setOf(":test:unit", ":feature"),
            androidTests = setOf(":test:unit", ":feature")
        )
    }

    @Test
    fun `change in the android test dependency - detects changes by androidTest dependencies`() {
        val sourceBranch = "changes-in-the-android-test-dependant-module"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
            file("test/ui/src/main/kotlin/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = setOf(":test:ui"),
            unitTests = setOf(":test:ui"),
            androidTests = setOf(":test:ui", ":feature")
        )
    }

    private fun detectChanges(sourceBranch: String, targetBranch: String): TestResult {
        return detectChangedModules(
            projectDir,
            "-Pci=true",
            "-PgitBranch=$sourceBranch",
            "-PtargetBranch=$targetBranch"
        )
    }
}
