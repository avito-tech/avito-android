package com.avito.android

import com.avito.test.gradle.KotlinModule
import com.avito.test.gradle.PlatformModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class PlatformDependencyTest {
    @Test
    fun `code ownership - skip platform module check`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = listOf(
                "com.avito.android.impact",
                "com.avito.android.code-ownership"
            ),
            modules = listOf(
                KotlinModule("a", dependencies = "compile platform(project(':b'))"),
                PlatformModule("b")
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            ":a:checkProjectDependenciesOwnership",
            "-Pavito.moduleOwnershipValidationEnabled=true",
            "-PgitBranch=xxx" // todo need for impact plugin
        ).assertThat().buildSuccessful()
    }
}
