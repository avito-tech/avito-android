package com.avito.android.bmp

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BuildMetricsPluginTest {

    private lateinit var projectDir: File

    @BeforeEach
    fun before(@TempDir dir: File) {
        projectDir = dir
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.bmp")
            }
        ).generateIn(projectDir)
    }

    @Test
    @Disabled
    fun test_1() {
        gradlew(
            projectDir,
            "help",
            configurationCache = true,
        ).assertThat().buildSuccessful()

        gradlew(
            projectDir,
            "help",
            configurationCache = true,
        ).assertThat().buildSuccessful()
    }

    @Test
    @Disabled
    fun test_2() {
        gradlew(
            projectDir,
            ":appA:assembleDebug",
            configurationCache = false,
        ).assertThat().buildSuccessful()
    }
}
