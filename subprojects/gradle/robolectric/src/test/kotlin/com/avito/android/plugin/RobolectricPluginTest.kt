package com.avito.android.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RobolectricPluginTest {

    @Test
    fun integration(@TempDir tempDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.robolectric")
            },
            modules = listOf(AndroidAppModule("app", enableKotlinAndroidPlugin = false))
        ).generateIn(tempDir)

        gradlew(tempDir, "help", "-PandroidXTestVersion=1.2.0").assertThat().buildSuccessful()
    }
}
