package com.avito.android.plugin.build_param_check

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
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
            plugins = listOf(
                "com.avito.android.buildchecks",
                "com.avito.android.impact"
            ),
            modules = listOf(
                AndroidAppModule(
                    name = "app-x",
                    packageName = "app_x_package",
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b")
                    )
                ),
                AndroidAppModule(
                    name = "app-y",
                    packageName = "app_y_package",
                    dependencies = setOf(
                        project(":lib-c"),
                        project(":lib-b")
                    )
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib_a_package"),
                AndroidLibModule(name = "lib-b", packageName = "lib_b_package"),
                AndroidLibModule(name = "lib-c", packageName = "lib_c_package")
            ),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    uniqueRClasses {}
                }
            """.trimIndent()
        ).generateIn(projectDir)

        val build = runCheck(expectFailure = false)

        build.assertThat().buildSuccessful()
    }

    @Test
    fun `fail - duplicated package in implementation`() {
        TestProjectGenerator(
            plugins = listOf(
                "com.avito.android.buildchecks",
                "com.avito.android.impact"
            ),
            modules = listOf(
                AndroidAppModule(
                    name = "app-x",
                    packageName = "app_x_package",
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b")
                    )
                ),
                AndroidAppModule(
                    name = "app-y",
                    packageName = "app_y_package",
                    dependencies = setOf(
                        project(":duplicate"),
                        project(":lib-b")
                    )
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib_a_package"),
                AndroidLibModule(name = "lib-b", packageName = "lib_b_package"),
                AndroidLibModule(name = "duplicate", packageName = "lib_b_package")
            ),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    uniqueRClasses {}
                }
            """.trimIndent()
        ).generateIn(projectDir)

        val build = runCheck(expectFailure = true)

        build.assertThat().buildFailed("Application :app-y has modules with the same package: [lib_b_package]")
    }

    /**
     * Test modules can also override resources in implementation modules
     * https://issuetracker.google.com/issues/175316324
     */
    @Test
    fun `fail - duplicated package in test`() {
        TestProjectGenerator(
            plugins = listOf(
                "com.avito.android.buildchecks",
                "com.avito.android.impact"
            ),
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = "app_package",
                    dependencies = setOf(
                        project(":lib"),
                        project(":lib-test", configuration = ANDROID_TEST_IMPLEMENTATION)
                    )
                ),
                AndroidLibModule(
                    name = "lib",
                    packageName = "lib_package"
                ),
                AndroidLibModule(
                    name = "lib-test",
                    packageName = "lib_package",
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

        build.assertThat().buildFailed("Application :app has modules with the same package: [lib_package]")
    }

    private fun runCheck(expectFailure: Boolean) = gradlew(
        projectDir,
        "checkUniqueAndroidPackages",
        "-PgitBranch=xxx", // todo need for impact plugin
        expectFailure = expectFailure
    )
}
