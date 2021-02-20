package com.avito.android.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RoomConfigPluginTest {

    @Test
    fun integration(@TempDir tempDir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.room-config")
            },
            modules = listOf(
                AndroidLibModule(
                    "lib",
                    plugins = plugins {
                        id("kotlin-kapt")
                    }
                )
            )
        ).generateIn(tempDir)

        gradlew(tempDir, "help", "-ParchPersistenceVersion=1.2.0").assertThat().buildSuccessful()
    }
}
