package com.avito.android.module_type.validation

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AndroidForbiddenDemoDependenciesTest {

    @Test
    fun `Android app rejects transitive forbidden dependency`(@TempDir projectDir: File) {
        generateProject(projectDir, allowForbidden = false)

        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains(":demo -> :bridge -> :heavy-module")
    }

    @Test
    fun `Android app allows declared forbidden dependency`(@TempDir projectDir: File) {
        generateProject(projectDir, allowForbidden = true)

        runCheck(projectDir, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    private fun runCheck(projectDir: File, expectFailure: Boolean) = gradlew(
        projectDir,
        ":demo:validateForbiddenDemoDependencies",
        expectFailure = expectFailure,
        useTestFixturesClasspath = true,
    )

    private fun generateProject(projectDir: File, allowForbidden: Boolean) {
        val allowance = if (allowForbidden) "allow(\":heavy-module\")" else ""
        TestProjectGenerator(
            useKts = true,
            plugins = plugins { id("com.avito.android.module-types-validator") },
            modules = listOf(
                AndroidAppModule(
                    name = "demo",
                    useKts = true,
                    imports = listOf(
                        "import com.avito.android.module_type.*",
                        "import com.avito.android.module_type.validation.ValidationExtension",
                    ),
                    plugins = plugins { id("com.avito.android.module-types-validator") },
                    dependencies = setOf(project(":bridge")),
                    buildGradleExtra = """
                        module {
                            type.set(ModuleType(StubApplication(), FunctionalType.DemoApp))
                            extensions.configure<ValidationExtension>("validation") {
                                forbiddenDemoDependencies {
                                    forbiddenDependencies(rootProject.file("forbidden-dependencies.txt"))
                                    $allowance
                                }
                            }
                        }
                    """.trimIndent(),
                ),
                AndroidLibModule(
                    name = "bridge",
                    useKts = true,
                    dependencies = setOf(project(":heavy-module")),
                ),
                KotlinModule(name = "heavy-module", packageName = "heavy", useKts = true),
            ),
        ).generateIn(projectDir)
        projectDir.resolve("forbidden-dependencies.txt").writeText(":heavy-module")
    }
}
