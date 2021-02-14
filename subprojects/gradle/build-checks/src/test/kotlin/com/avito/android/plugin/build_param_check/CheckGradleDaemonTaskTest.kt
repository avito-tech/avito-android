package com.avito.android.plugin.build_param_check

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CheckGradleDaemonTaskTest {

    @Test
    fun `checkGradleDaemon - passes - when no buildSrc in project`(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.buildchecks")
            },
            buildGradleExtra = """
                buildChecks {
                    enableByDefault = false
                    gradleDaemon {}
                }
            """.trimIndent()
        )
            .generateIn(projectDir)

        gradlew(projectDir, ":checkGradleDaemon")
    }
}
