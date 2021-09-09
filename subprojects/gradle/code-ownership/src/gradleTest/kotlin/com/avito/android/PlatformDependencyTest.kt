package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.platformProject
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.module.PlatformModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class PlatformDependencyTest {

    @Test
    fun `code ownership - skip platform module check`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
                id("com.avito.android.code-ownership")
            },
            modules = listOf(
                KotlinModule(
                    "lib",
                    dependencies = setOf(
                        platformProject(
                            path = ":platform",
                            configuration = IMPLEMENTATION
                        )
                    )
                ),
                PlatformModule("platform")
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            "checkProjectDependenciesOwnership",
            "-Pavito.moduleOwnershipValidationEnabled=true",
            "-PgitBranch=xxx" // todo need for impact plugin
        ).assertThat().buildSuccessful()
    }
}
