package com.avito.android.build_checks

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

class UniqueRClassesTest {

    private lateinit var projectDir: File

    @BeforeEach
    internal fun setUp(@TempDir projectDir: File) {
        this.projectDir = projectDir
    }

    @Test
    fun `success - unique package names`() {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.buildchecks")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app-x",
                    packageName = "app.x.package",
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b")
                    )
                ),
                AndroidAppModule(
                    name = "app-y",
                    packageName = "app.y.package",
                    dependencies = setOf(
                        project(":lib-c"),
                        project(":lib-b")
                    )
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib.a.package"),
                AndroidLibModule(name = "lib-b", packageName = "lib.b.package"),
                AndroidLibModule(name = "lib-c", packageName = "lib.c.package")
            ),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    uniqueRClasses {}
                }
            """.trimIndent()
        ).generateIn(projectDir)

        val build = runCheck()

        build.assertThat().buildSuccessful()
    }

    @Test
    fun `success - allowed duplicate`() {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.buildchecks")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app-x",
                    packageName = "app.x.package",
                    dependencies = setOf(
                        project(":lib-a"),
                    )
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
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    uniqueRClasses {
                        allowedNonUniquePackageNames.add("lib.package")
                    }
                }
            """.trimIndent()
        ).generateIn(projectDir)

        val build = runCheck()

        build.assertThat().buildSuccessful()
    }

    @Test
    fun `fail - duplicated package in implementation`() {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.buildchecks")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app-x",
                    packageName = "app.x.package",
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b")
                    )
                ),
                AndroidAppModule(
                    name = "app-y",
                    packageName = "app.y.package",
                    dependencies = setOf(
                        project(":duplicate"),
                        project(":lib-b")
                    )
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib.a.package"),
                AndroidLibModule(name = "lib-b", packageName = "lib.b.package"),
                AndroidLibModule(name = "duplicate", packageName = "lib.b.package")
            ),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    uniqueRClasses {}
                }
            """.trimIndent()
        ).generateIn(projectDir)

        val build = runCheck(expectFailure = true)

        build.assertThat().buildFailed("Application :app-y has modules with the same package: [lib.b.package]")
    }

    /**
     * Test modules can also override resources in implementation modules
     * https://issuetracker.google.com/issues/175316324
     */
    @Test
    fun `fail - duplicated package in test`() {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.buildchecks")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = "app.package",
                    dependencies = setOf(
                        project(":lib"),
                        project(":lib-test", configuration = ANDROID_TEST_IMPLEMENTATION)
                    )
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
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    uniqueRClasses {}
                }
            """.trimIndent()
        ).generateIn(projectDir)

        val build = runCheck(expectFailure = true)

        build.assertThat().buildFailed("Application :app has modules with the same package: [lib.package]")
    }

    private fun runCheck(expectFailure: Boolean = false) = gradlew(
        projectDir,
        "checkBuildEnvironment",
        "-PgitBranch=xxx", // todo need for impact plugin
        expectFailure = expectFailure
    )
}
