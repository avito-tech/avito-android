package com.avito.impact.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ImpactPluginTest {

    @Test
    fun simpleIntegration(@TempDir projectDir: File) {
        TestProjectGenerator(plugins = listOf("com.avito.android.impact")).generateIn(projectDir)

        gradlew(projectDir, "help").assertThat().buildSuccessful()
    }
}
