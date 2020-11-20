package com.avito.android.plugin.build_param_check

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class UniqueRClassesTest {

    @Test
    fun `unique package names`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf(
                "com.avito.android.buildchecks",
                "com.avito.android.impact"
            ),
            modules = listOf(
                AndroidAppModule(
                    name = "app-x",
                    packageName = "x",
                    dependencies = """
                        implementation project(':lib-a')
                        implementation project(':lib-b')
                    """.trimIndent()
                ),
                AndroidAppModule(
                    name = "app-y",
                    packageName = "y",
                    dependencies = """
                        implementation project(':lib-b')
                        implementation project(':lib-c')
                    """.trimIndent()
                ),
                AndroidLibModule(name = "lib-a", packageName = "a"),
                AndroidLibModule(name = "lib-b", packageName = "b"),
                AndroidLibModule(name = "lib-c", packageName = "c")
            ),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    uniqueRClasses {}
                }
            """.trimIndent()
        ).generateIn(projectDir)

        val build = gradlew(
            projectDir,
            "checkUniqueAndroidPackages",
            "-PgitBranch=xxx" // todo need for impact plugin
        )
        build.assertThat().buildSuccessful()
    }

    @Test
    fun `duplicated package names`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf(
                "com.avito.android.buildchecks",
                "com.avito.android.impact"
            ),
            modules = listOf(
                AndroidAppModule(
                    name = "app-x",
                    packageName = "x",
                    dependencies = """
                        implementation project(':lib-a')
                        implementation project(':lib-b')
                    """.trimIndent()
                ),
                AndroidAppModule(
                    name = "app-y",
                    packageName = "y",
                    dependencies = """
                        implementation project(':lib-b')
                        implementation project(':duplicate')
                    """.trimIndent()
                ),
                AndroidLibModule(name = "lib-a", packageName = "a"),
                AndroidLibModule(name = "lib-b", packageName = "b"),
                AndroidLibModule(name = "duplicate", packageName = "b")
            ),
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    uniqueRClasses {}
                }
            """.trimIndent()
        ).generateIn(projectDir)

        val build = gradlew(
            projectDir,
            "checkUniqueAndroidPackages",
            "-PgitBranch=xxx",
            expectFailure = true
        )
        build.assertThat().buildFailed("Application :app-y has modules with the same package: [b]")
    }
}
