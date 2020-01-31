package com.avito.android.plugin

import com.avito.test.gradle.AndroidAppModule
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RobolectricPluginTest {

    @Test
    fun integration(@TempDir tempDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.robolectric"),
            modules = listOf(AndroidAppModule("app"))
        ).generateIn(tempDir)

        gradlew(tempDir, "help", "-PandroidXTestVersion=1.2.0").assertThat().buildSuccessful()
    }
}
