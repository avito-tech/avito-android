package com.avito.ci

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
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

class ImpactAnalysisTest {

    private lateinit var projectDir: File

    private val projectGenerator = TestProjectGenerator(
        plugins = listOf("com.avito.android.impact"),
        modules = listOf(
            AndroidAppModule(
                "app", dependencies = """
                        implementation project(':feature_a')
                        implementation project(':feature_b')
                        implementation project(':feature_c')
                        androidTestImplementation project(':test_core')
                    """
            ),
            AndroidLibModule(
                "feature_a", dependencies = """
                        implementation project(':api')
                        androidTestImplementation project(':test_feature_a')
                    """
            ),
            AndroidLibModule(
                "feature_b", dependencies = """
                        implementation project(':api')
                    """
            ),
            AndroidLibModule(
                "feature_c"
            ),
            AndroidLibModule("api"),
            AndroidLibModule(
                "test_feature_a",
                dependencies = """
                       implementation project(':test_core') 
                    """
            ),
            AndroidLibModule(
                name = "test_core"
            )
        )
    )

    private val allModules = projectGenerator.modules.map { it.name }.toSet()

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()

        projectGenerator.generateIn(projectDir)

        with(projectDir) {
            git("checkout -b develop")
        }
    }

    @Test
    fun `no changes at all - detects changes in all paths`() {
        val result = detectChanges("develop", "develop")

        result.assertMarkedModules(
            projectDir,
            implementation = allModules,
            unitTests = allModules,
            androidTests = allModules
        )
    }

    @Test
    fun `no changes at all but in branch - doesn't detect changed modules`() {
        val sourceBranch = "no-changes"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b no-changes develop")
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
    fun `changes in the root project (not committed) - detects changes in the root project`() {
        val sourceBranch = "changes-in-the-root-project"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b changes-in-the-root-project develop")
            file("app/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
        }
        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("app"),
            unitTests = setOf("app"),
            androidTests = setOf("app")
        )
    }

    @Test
    fun `changes in the app module (committed) - detects changes in the app module`() {
        val sourceBranch = "changes-in-the-root-project"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
            file("app/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }

        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("app"),
            unitTests = setOf("app"),
            androidTests = setOf("app")
        )
    }

    @Test
    fun `changes in the feature - detects changes in the feature and its dependents`() {
        val sourceBranch = "changes-in-the-feature"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
            file("feature_a/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("feature_a", "app"),
            unitTests = setOf("feature_a", "app"),
            androidTests = setOf("feature_a", "app")
        )
    }

    @Test
    fun `changes in the transitive dependency - detects changes in all paths`() {
        val sourceBranch = "changes-in-the-api"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
            file("api/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("app", "feature_a", "feature_b", "api"),
            unitTests = setOf("app", "feature_a", "feature_b", "api"),
            androidTests = setOf("app", "feature_a", "feature_b", "api")
        )
    }

    @Test
    fun `changes in the transitive androidTest dependency - detects androidTest changes in app`() {
        val sourceBranch = "changes-in-the-android-test"
        val targetBranch = "develop"
        with(projectDir) {
            git("checkout -b $sourceBranch $targetBranch")
            file("test_core/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges(sourceBranch, targetBranch)

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("test_feature_a", "test_core"),
            unitTests = setOf("test_feature_a", "test_core"),
            androidTests = setOf("app", "feature_a", "test_feature_a", "test_core")
        )
    }

    private fun detectChanges(sourceBranch: String, targetBranch: String): TestResult {
        return detectChangedModules(
            projectDir,
            "-Pci=true",
            "-PgitBranch=$sourceBranch",
            "-PtargetBranch=$targetBranch",
            "--stacktrace"
        )
    }
}
