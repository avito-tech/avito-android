package com.avito.android.plugin.build_param_check

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class CheckGradleDaemonTaskTest {

    @Disabled("can't test single check atm #61")
    @Test
    fun `checkGradleDaemon - passes - when no buildSrc in project`(@TempDir projectDir: File) {
        TestProjectGenerator(plugins = listOf("com.avito.android.buildchecks")).generateIn(projectDir)

        gradlew(projectDir, ":checkGradleDaemon")
    }
}
