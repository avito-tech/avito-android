package com.avito.android

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.AndroidLibModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.git
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class ModuleTypesPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun setup(@TempDir tempPath: Path) {
        projectDir = tempPath.toFile()
    }

    @Test
    fun `android application - having library dependencies in implementation configuration in library module - has checkProjectDependenciesTypeTask scheduled`() {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.impact"),
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = listOf("com.avito.android.module-types")
                ),
                AndroidLibModule(
                    "feature",
                    plugins = listOf("com.avito.android.module-types"),
                    dependencies = "implementation project(':dependent_test_module')"
                ),
                AndroidLibModule(
                    "dependent_test_module",
                    plugins = listOf("com.avito.android.module-types")
                )
            )
        ).generateIn(projectDir)

        with(projectDir) {
            git("checkout -b develop")
        }

        val result = gradlew(
            projectDir, "assemble",
            "-Pavito.moduleTypeValidationEnabled=true",
            "-PgitBranch=xxx", // todo need for impact plugin
            dryRun = true
        )
        result.assertThat()
            .tasksShouldBeTriggered(
                ":dependent_test_module:checkProjectDependenciesType",
                ":feature:checkProjectDependenciesType"
            )
    }
}
