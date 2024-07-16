package com.avito.test.summary

import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ConfigurationTest {

    @Test
    fun `configuration with applied plugin - ok`(@TempDir projectDir: File) {
        MinimalTestSummaryProject.builder(projectDir.file("test_summary_destination.json")).generateIn(projectDir)

        gradlew(projectDir, "tasks").assertThat().buildSuccessful()
    }
}
