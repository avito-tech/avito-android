package com.avito.android

import com.avito.android.module_graph.models.ModuleGraphInfo
import com.avito.android.module_graph.models.ModuleLinesOfCode
import com.avito.android.module_type.FunctionalType
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class DemoAppMetricsPluginGradleTest {

    @Test
    fun `generateModuleGraph - writes applications and own lines of code`(
        @TempDir projectDir: File,
    ) {
        generateProject(projectDir)

        val result = gradlew(projectDir, "generateModuleGraph")

        result.assertThat().buildSuccessful()
        val moduleGraphInfo = Json.decodeFromString<ModuleGraphInfo>(
            projectDir.resolve("build/module-graph.json").readText()
        )
        assertThat(moduleGraphInfo.applications).containsExactly(":demo-app")
        assertThat(moduleGraphInfo.linesOfCode).containsExactly(
            ":demo-app",
            ModuleLinesOfCode(main = 2, test = 1, androidTest = 1),
            ":library",
            ModuleLinesOfCode(main = 2),
        )
    }

    @Test
    fun `generateModuleGraph - recalculates lines of code after source changes`(
        @TempDir projectDir: File,
    ) {
        generateProject(projectDir)
        gradlew(projectDir, "generateModuleGraph").assertThat().buildSuccessful()

        projectDir.file(
            "library/src/main/kotlin/AdditionalSource.kt",
            "class AdditionalSource",
        )

        gradlew(projectDir, "generateModuleGraph").assertThat().buildSuccessful()
        val moduleGraphInfo = Json.decodeFromString<ModuleGraphInfo>(
            projectDir.resolve("build/module-graph.json").readText()
        )
        assertThat(moduleGraphInfo.linesOfCode.getValue(":library").main).isEqualTo(3)
    }

    @Test
    fun `generateModuleGraph - explains how to install missing cloc`(
        @TempDir projectDir: File,
    ) {
        generateProject(projectDir)
        val emptyPath = projectDir.resolve("empty-path").apply(File::mkdirs)

        gradlew(
            projectDir,
            "generateModuleGraph",
            expectFailure = true,
            environment = System.getenv() + ("PATH" to emptyPath.absolutePath),
        ).assertThat()
            .buildFailed()
            .outputContains(
                "cloc not found. Install it and make it available on PATH, " +
                    "for example: brew install cloc"
            )
    }

    private fun generateProject(projectDir: File) {
        TestProjectGenerator(
            name = "module-graph-fixture",
            plugins = plugins {
                id("com.avito.android.module-types")
                id("com.avito.android.demo-app-metrics")
            },
            modules = listOf(
                module(
                    name = "demo-app",
                    functionalType = FunctionalType.DemoApp,
                    dependencies = setOf(project(":library")),
                ),
                module(
                    name = "library",
                    functionalType = FunctionalType.Public,
                ),
            ),
            useKts = true,
        ).generateIn(projectDir)
    }

    private fun module(
        name: String,
        functionalType: FunctionalType,
        dependencies: Set<GradleDependency> = emptySet(),
    ): KotlinModule = KotlinModule(
        name = name,
        packageName = "com.${name.replace("-", "")}",
        imports = listOf("import com.avito.android.module_type.*"),
        plugins = plugins {
            id("com.avito.android.module-types")
            id("com.avito.android.demo-app-metrics")
        },
        buildGradleExtra = """
            class FixtureApplication : ApplicationDeclaration {
                override val name: String = "fixture"
            }

            module {
                type.set(ModuleType(FixtureApplication(), FunctionalType.${functionalType.name}))
            }
        """.trimIndent(),
        dependencies = dependencies,
        useKts = true,
        mutator = {
            file("src/test/kotlin/TestSource.kt", "class TestSource")
            file("src/androidTest/kotlin/AndroidTestSource.kt", "class AndroidTestSource")
        },
    )
}
