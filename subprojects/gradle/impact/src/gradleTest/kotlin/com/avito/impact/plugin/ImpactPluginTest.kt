package com.avito.impact.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ImpactPluginTest {

    @Test
    fun simpleIntegration(@TempDir projectDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.impact")
            }
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            "-PgitBranch=xxx", // todo need for impact plugin
            "help"
        ).assertThat().buildSuccessful()
    }
}
