package com.avito.android.build_checks.unique_r

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class UniqueRClassesTest {

    private lateinit var projectDir: File

    @BeforeEach
    internal fun setUp(@TempDir projectDir: File) {
        this.projectDir = projectDir
    }

    @Test
    fun `success - unique package names`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b"),
                        project(":lib-c"),
                    ),
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueRClasses {}
                        }
                        """.trimIndent()
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib.a"),
                AndroidLibModule(name = "lib-b", packageName = "lib.b"),
                AndroidLibModule(name = "lib-c", packageName = "lib.c")
            ),
        ).generateIn(projectDir)

        val build = runCheck()

        build.assertThat().buildSuccessful()
        build.assertThat().tasksShouldBeTriggered(":app:checkUniqueAndroidPackages")
    }

    @Test
    fun `success - allowed duplicate`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    dependencies = setOf(
                        project(":lib-a"),
                    ),
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueRClasses {
                                allowedNonUniquePackageNames.add("lib.package")
                            }
                        }
                        """.trimIndent()
                ),
                AndroidLibModule(
                    name = "lib-a",
                    packageName = "lib.package",
                    dependencies = setOf(
                        project(":lib-b")
                    )
                ),
                AndroidLibModule(
                    name = "lib-b",
                    packageName = "lib.package"
                ),
            ),
        ).generateIn(projectDir)

        val build = runCheck()

        build.assertThat().buildSuccessful()
        build.assertThat().tasksShouldBeTriggered(":app:checkUniqueAndroidPackages")
    }

    @Test
    fun `fail - duplicated package in implementation`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":duplicate")
                    ),
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueRClasses {}
                        }
                    """.trimIndent()
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib.a"),
                AndroidLibModule(name = "duplicate", packageName = "lib.a")
            ),
        ).generateIn(projectDir)

        val build = runCheck(expectFailure = true)

        build.assertThat()
            .buildFailed()
            .outputContains("Application :app has dependencies with the same package in AndroidManifest.xml: [lib.a]")
    }

    /**
     * Test modules can also override resources in implementation modules
     * https://issuetracker.google.com/issues/175316324
     */
    @Test
    fun `fail - duplicated package in test`() {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    dependencies = setOf(
                        project(":lib"),
                        project(":lib-test", configuration = ANDROID_TEST_IMPLEMENTATION)
                    ),
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueRClasses {}
                        }
                    """.trimIndent()
                ),
                AndroidLibModule(
                    name = "lib",
                    packageName = "lib.package"
                ),
                AndroidLibModule(
                    name = "lib-test",
                    packageName = "lib.package",
                    dependencies = setOf(
                        project(":lib")
                    )
                )
            ),
        ).generateIn(projectDir)

        val build = runCheck(expectFailure = true)

        @Suppress("MaxLineLength")
        build.assertThat()
            .buildFailed()
            .outputContains("Application :app has dependencies with the same package in AndroidManifest.xml: [lib.package]")
    }

    private fun runCheck(expectFailure: Boolean = false) = gradlew(
        projectDir,
        "checkBuildEnvironment",
        expectFailure = expectFailure,
    )
}
