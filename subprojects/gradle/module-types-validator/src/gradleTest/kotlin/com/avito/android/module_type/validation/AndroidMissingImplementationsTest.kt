package com.avito.android.module_type.validation

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.API
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.FolderModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class AndroidMissingImplementationsTest {

    @Test
    fun `Android app with public dependency requires fake implementation`(@TempDir projectDir: File) {
        generateProject(projectDir, fakeConfiguration = null)

        runCheck(projectDir, expectFailure = true)
            .assertThat()
            .buildFailed()
            .outputContains("project(\":service:fake\")")
    }

    @Test
    fun `Android app with public and fake dependencies succeeds`(@TempDir projectDir: File) {
        generateProject(projectDir, fakeConfiguration = IMPLEMENTATION)

        runCheck(projectDir, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    @Test
    fun `Android app with fake dependency in androidTest succeeds`(@TempDir projectDir: File) {
        generateProject(projectDir, fakeConfiguration = ANDROID_TEST_IMPLEMENTATION)

        runCheck(projectDir, expectFailure = false)
            .assertThat()
            .buildSuccessful()
    }

    private fun runCheck(projectDir: File, expectFailure: Boolean) = gradlew(
        projectDir,
        "validateMissingImplementations",
        expectFailure = expectFailure,
        useTestFixturesClasspath = true,
    )

    private fun generateProject(projectDir: File, fakeConfiguration: CONFIGURATION?) {
        TestProjectGenerator(
            useKts = true,
            plugins = plugins { id("com.avito.android.module-types-validator") },
            modules = listOf(
                FolderModule(
                    name = "service",
                    modules = listOf(
                        KotlinModule(name = "public", packageName = "service.api", useKts = true),
                        KotlinModule(
                            name = "fake",
                            useKts = true,
                            dependencies = setOf(project(":service:public", API)),
                        ),
                    ),
                ),
                AndroidAppModule(
                    name = "demo",
                    useKts = true,
                    imports = listOf("import com.avito.android.module_type.*"),
                    plugins = plugins { id("com.avito.android.module-types-validator") },
                    dependencies = setOfNotNull(
                        project(":service:public"),
                        fakeConfiguration?.let { project(":service:fake", it) },
                    ),
                    buildGradleExtra = """
                        module { type.set(ModuleType(StubApplication(), FunctionalType.DemoApp)) }
                    """.trimIndent(),
                ),
            ),
        ).generateIn(projectDir)
    }
}
