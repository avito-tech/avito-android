package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class MisleadingExceptionTest {

    /**
     * Problem was that ownership plugin reported misconfiguration in afterEvaluation phase even if everything is OK
     * with ownership config, but script failed somewhere
     *
     * @see MBSA-710
     */
    @Test
    fun `script compilation error - does not lead to ownership missing error`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            modules = listOf(
                AndroidLibModule(
                    name = "lib",
                    buildGradleExtra = """
                        |SOME_BAD_CODE // compile error here
                        |
                        |enum Owners { Speed }
                        |
                        |ownership {
                        |    owners(Owners.Speed)
                        |}
                    """.trimMargin(),
                    useKts = false
                )
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir, "help",
            "-Pavito.ownership.strictOwnership=true",
            dryRun = false,
            expectFailure = true
        ).assertThat()
            .buildFailed()
            .outputContains("Could not get unknown property 'SOME_BAD_CODE'")
            .outputDoesNotContain("Owners must be set for the :lib project")
    }
}
