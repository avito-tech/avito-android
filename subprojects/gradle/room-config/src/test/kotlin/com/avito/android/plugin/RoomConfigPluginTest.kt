package com.avito.android.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RoomConfigPluginTest {

    @Test
    fun integration(@TempDir tempDir: File) {
        TestProjectGenerator(
            plugins = listOf("com.avito.android.room-config"),
            modules = listOf(AndroidLibModule("lib", plugins = listOf("kotlin-kapt")))
        ).generateIn(tempDir)

        gradlew(tempDir, "help", "-ParchPersistenceVersion=1.2.0").assertThat().buildSuccessful()
    }
}
