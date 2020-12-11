package com.avito.impact

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.TestResult
import com.avito.test.gradle.commit
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.API
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.TEST_IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.platformProject
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.git
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.Module
import com.avito.test.gradle.module.ParentGradleModule
import com.avito.test.gradle.module.PlatformModule
import com.avito.test.gradle.mutate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ImpactAnalysisTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempDir: Path) {
        projectDir = tempDir.toFile()
    }

    @Disabled("TODO: it seems here must be no changes")
    @Test
    fun `no changed modules - no changed files on the same branch`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("app")
            )
        )
        val result = detectChanges(sourceBranch = TARGET_BRANCH)

        result.assertMarkedModules(
            projectDir,
            implementation = emptySet(),
            unitTests = emptySet(),
            androidTests = emptySet()
        )
    }

    @Test
    fun `no changed modules - no changed files on another branch`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("app")
            )
        )
        checkoutSourceBranch()

        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = emptySet(),
            unitTests = emptySet(),
            androidTests = emptySet()
        )
    }

    @Test
    fun `no changed modules - changed only ignored files`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("app")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file(
                name = ".tia_ignore",
                content = """
                .tia_ignore
                *.md
                ignored_directory/*
                """.trimIndent()
            )
            file("README.md")
            dir("ignored_directory") {
                file("KotlinClass.kt")
            }
            dir("app") {
                file("README.md")
            }
            commit()
        }

        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = emptySet(),
            unitTests = emptySet(),
            androidTests = emptySet()
        )
    }

    @Test
    fun `all modules changed as fallback - unknown change`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("a"),
                AndroidAppModule("b")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("unknown-new.properties")
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("a", "b"),
            unitTests = setOf("a", "b"),
            androidTests = setOf("a", "b")
        )
    }

    @Test
    fun `changed module - changed sources (not committed)`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("app"),
                AndroidAppModule("standalone_app")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("app/$KOTLIN_SOURCE_SET/com/app/SomeClass.kt").mutate()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("app"),
            unitTests = setOf("app"),
            androidTests = setOf("app")
        )
    }

    @Test
    fun `changed module - changed sources (committed)`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("app"),
                AndroidAppModule("standalone_app")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("app/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }

        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("app"),
            unitTests = setOf("app"),
            androidTests = setOf("app")
        )
    }

    @Test
    fun `transitive changes - changed implementation dependency`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        project(":feature_a"),
                        project(":feature_b")
                    )
                ),
                AndroidLibModule("feature_a"),
                AndroidLibModule("feature_b")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("feature_a/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("feature_a", "app"),
            unitTests = setOf("feature_a", "app"),
            androidTests = setOf("feature_a", "app")
        )
    }

    @Test
    fun `transitive changes - changed transitive implementation dependency`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        project(":feature_a"),
                        project(":feature_b")
                    )
                ),
                AndroidLibModule(
                    name = "feature_a",
                    dependencies = setOf(
                        project(":core")
                    )
                ),
                AndroidLibModule(
                    name = "feature_b",
                    dependencies = setOf(
                        project(":core")
                    )
                ),
                AndroidLibModule("core")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("core/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("app", "feature_a", "feature_b", "core"),
            unitTests = setOf("app", "feature_a", "feature_b", "core"),
            androidTests = setOf("app", "feature_a", "feature_b", "core")
        )
    }

    @Test
    fun `transitive changes - changed transitive test dependency`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        project(path = ":feature", configuration = IMPLEMENTATION),
                        project(path = ":test_utils", configuration = TEST_IMPLEMENTATION)
                    )
                ),
                AndroidLibModule(
                    name = "feature",
                    dependencies = setOf(
                        project(path = ":test_utils", configuration = TEST_IMPLEMENTATION)
                    )
                ),
                AndroidLibModule("test_utils")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("test_utils/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("test_utils"),
            unitTests = setOf("test_utils", "feature", "app"),
            androidTests = setOf("test_utils")
        )
    }

    @Test
    fun `transitive changes - changed transitive androidTest dependency`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        project(path = ":android_test_utils", configuration = ANDROID_TEST_IMPLEMENTATION)
                    )

                ),
                AndroidLibModule(
                    name = "android_test_utils",
                    dependencies = setOf(
                        project(":core_test_utils")
                    )
                ),
                AndroidLibModule("core_test_utils")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("core_test_utils/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("android_test_utils", "core_test_utils"),
            unitTests = setOf("android_test_utils", "core_test_utils"),
            androidTests = setOf("app", "android_test_utils", "core_test_utils")
        )
    }

    // Checking problems with circular dependencies.
    // Gradle has no problems but we build graph manually.
    // https://avito-tech.github.io/avito-android/docs/architecture/modules/#modules-for-test-fixtures
    @Test
    fun `transitive changes - changed androidTest fixtures`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        project(path = ":feature", configuration = IMPLEMENTATION),
                        project(path = ":android_test_feature", configuration = ANDROID_TEST_IMPLEMENTATION)
                    )
                ),
                AndroidLibModule(
                    name = "feature",
                    dependencies = setOf(
                        project(path = ":android_test_feature", configuration = ANDROID_TEST_IMPLEMENTATION)
                    )
                ),
                AndroidLibModule(
                    name = "android_test_feature",
                    dependencies = setOf(
                        project(":feature")
                    )
                )
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("android_test_feature/$KOTLIN_SOURCE_SET/SomeClass.kt").mutate()
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("android_test_feature"),
            unitTests = setOf("android_test_feature"),
            androidTests = setOf("android_test_feature", "feature", "app")
        )
    }

    @Test
    fun `transitive changes - changed implementation platform dependency`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        platformProject(path = ":platform", configuration = API),
                        project(path = ":feature", configuration = IMPLEMENTATION)
                    )
                ),
                AndroidLibModule(
                    name = "feature",
                    dependencies = setOf(
                        platformProject(path = ":platform", configuration = API)
                    )
                ),
                PlatformModule(name = "platform")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("platform/build.gradle").appendText("// changes")
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("feature", "app"),
            unitTests = setOf("feature", "app"),
            androidTests = setOf("feature", "app")
        )
    }

    /**
     * it could be improved, but for now parent/build.gradle is just a fallback
     */
    @Test
    fun `all child and dependent modules changed - parent module gradle configuration changed`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        project(":parent:feature1"),
                        project(":parent:feature2")
                    )
                ),
                ParentGradleModule(
                    name = "parent",
                    modules = listOf(
                        AndroidLibModule(
                            "feature1"
                        ),
                        AndroidLibModule(
                            "feature2"
                        )
                    )
                )
            )
        )

        with(projectDir) {
            checkoutSourceBranch()
            file("parent/build.gradle").appendText("\nprintln('changed')")
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("parent:feature1", "parent:feature2", "app", "standalone_app"),
            unitTests = setOf("parent:feature1", "parent:feature2", "app", "standalone_app"),
            androidTests = setOf("parent:feature1", "parent:feature2", "app", "standalone_app")
        )
    }

    @Test
    fun `all modules changed - root gradle configuration changed`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        project(":feature")
                    )
                ),
                AndroidLibModule(
                    "feature"
                )
            )
        )

        with(projectDir) {
            checkoutSourceBranch()
            file("build.gradle").appendText("\nprintln('changed')")
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf("feature", "standalone_app", "app"),
            unitTests = setOf("feature", "standalone_app", "app"),
            androidTests = setOf("feature", "standalone_app", "app")
        )
    }

    @Test
    fun `transitive changes - changed androidTest platform dependency`() {
        generateProject(
            modules = listOf(
                AndroidAppModule("standalone_app"),
                AndroidAppModule(
                    name = "app",
                    dependencies = setOf(
                        platformProject(path = ":platform", configuration = ANDROID_TEST_IMPLEMENTATION)
                    )
                ),
                PlatformModule(name = "platform")
            )
        )
        with(projectDir) {
            checkoutSourceBranch()
            file("platform/build.gradle").appendText("// changes")
            commit()
        }
        val result = detectChanges()

        result.assertMarkedModules(
            projectDir,
            implementation = setOf(),
            unitTests = setOf(),
            androidTests = setOf("app")
        )
    }

    private fun generateProject(modules: List<Module>) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = modules
        ).generateIn(projectDir)

        checkoutTargetBranch()
    }

    private fun checkoutSourceBranch() =
        projectDir.git("checkout -b $SOURCE_BRANCH")

    private fun checkoutTargetBranch() =
        projectDir.git("checkout -b $TARGET_BRANCH")

    private fun detectChanges(sourceBranch: String = SOURCE_BRANCH, targetBranch: String = TARGET_BRANCH): TestResult {
        return detectChangedModules(
            projectDir,
            "-Pci=true",
            "-PgitBranch=$sourceBranch",
            "-PtargetBranch=$targetBranch",
            "--stacktrace"
        )
    }
}

private const val SOURCE_BRANCH = "feature"
private const val TARGET_BRANCH = "develop"
private const val KOTLIN_SOURCE_SET = "src/main/kotlin"
