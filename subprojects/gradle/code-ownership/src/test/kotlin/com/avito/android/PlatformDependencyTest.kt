package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.module.PlatformModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class PlatformDependencyTest {

    @Test
    fun `code ownership - skip platform module check`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf(
                "com.avito.android.impact",
                "com.avito.android.code-ownership"
            ),
            modules = listOf(
                KotlinModule(
                    "lib",
                    dependencies = "compile platform(project(':platform'))"
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
