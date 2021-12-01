package com.avito.impact.plugin

import com.avito.impact.assertMarkedModules
import com.avito.impact.detectChangedModules
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.commit
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.avito.test.gradle.kotlinClass
import com.avito.test.gradle.module
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.mutate
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

/**
 * app(
 *  implementation(":feature")
 * )
 *
 * feature {
 *  src/
 *   release/kotlin/Feature.kt
 *   test/kotlin/FeatureTest.kt
 *   testRelease/kotlin/FeatureTest.kt
 *   androidTest/kotlin/ScreenTest.kt
 *   testUtils/kotlin/SharedTestUtils.kt
 * }
 */
internal class SourceSetsImpactTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()

        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(project(":feature"))
                ),
                AndroidLibModule(
                    name = "feature",
                    buildGradleExtra = """
                        android {
                            sourceSets {
                                String sharedTestDir = 'src/testUtils/java'
                                test.java.srcDirs += sharedTestDir
                                androidTest.java.srcDirs += sharedTestDir
                            }
                        }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        with(projectDir) {
            module("feature") {
                dir("src/release/kotlin") {
                    kotlinClass("Feature")
                }
                dir("src/test/kotlin") {
                    kotlinClass("FeatureTest")
                }
                dir("src/testRelease/kotlin") {
                    kotlinClass("FeatureTest")
                }
                dir("src/androidTest/kotlin") {
                    kotlinClass("ScreenTest")
                }
                dir("src/testUtils/java") {
                    kotlinClass("SharedTestUtils")
                }
                file("unknown.properties")
            }
        }

        with(projectDir) {
            git("checkout -b develop")
            commit()
        }

        with(projectDir) {
            git("checkout -b new_branch develop")
        }
    }

    @Test
    fun `change in the implementation main - detects changes in all tests and dependant projects`() {
        with(projectDir) {
            git("reset --hard")
            file("feature/src/main/kotlin/com/feature/SomeClass.kt").mutate()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf(":feature", ":app"),
            unitTests = setOf(":feature", ":app"),
            androidTests = setOf(":feature", ":app")
        )
    }

    @Test
    fun `change in the implementation build type - detects changes in all tests and dependant projects`() {
        with(projectDir) {
            git("reset --hard")
            file("feature/src/release/kotlin/Feature.kt").mutate()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf(":feature", ":app"),
            unitTests = setOf(":feature", ":app"),
            androidTests = setOf(":feature", ":app")
        )
    }

    @Test
    fun `change an orphan file - detects changes as in the implementation`() {
        with(projectDir) {
            git("reset --hard")
            file("feature/unknown.properties").mutate()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf(":feature", ":app"),
            unitTests = setOf(":feature", ":app"),
            androidTests = setOf(":feature", ":app")
        )
    }

    @Test
    fun `change in unit tests main - detects changes in unit tests only in this module`() {
        with(projectDir) {
            git("reset --hard")
            file("feature/src/test/kotlin/FeatureTest.kt").mutate()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = emptySet(),
            unitTests = setOf(":feature"),
            androidTests = emptySet()
        )
    }

    @Test
    fun `change in unit tests build type - detects changes in unit tests only in this module`() {
        with(projectDir) {
            git("reset --hard")
            file("feature/src/testRelease/kotlin/FeatureTest.kt").mutate()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = emptySet(),
            unitTests = setOf(":feature"),
            androidTests = emptySet()
        )
    }

    @Test
    fun `change in android tests - detects changes in android tests only in this module`() {
        with(projectDir) {
            git("reset --hard")
            file("feature/src/androidTest/kotlin/ScreenTest.kt").mutate()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = emptySet(),
            unitTests = emptySet(),
            androidTests = setOf(":feature")
        )
    }

    @Test
    fun `change in a custom source set for tests - detects changes in dependant test source sets`() {
        with(projectDir) {
            git("reset --hard")
            file("feature/src/testUtils/java/SharedTestUtils.kt").mutate()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = emptySet(),
            unitTests = setOf(":feature"),
            androidTests = setOf(":feature")
        )
    }

    private fun detectChanges(): TestResult = detectChangedModules(
        projectDir,
        "-Pci=true",
        "-PgitBranch=new_branch",
        "-PtargetBranch=develop"
    )
}
