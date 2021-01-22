package com.avito.android.plugin

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class RobolectricConventionPluginTest {

    @Test
    fun `configuration - ok - plugin applied dry run`(@TempDir projectDir: File) {
        TestProjectGenerator(
            modules = listOf(
                AndroidAppModule(
                    "app",
                    enableKotlinAndroidPlugin = false,
                    plugins = listOf("com.avito.android.robolectric-convention"),
                    buildGradleExtra = """
                        robolectricConvention {
                            androidXTestVersion = "1.0.0"
                            avitoRobolectricLibVersion = "2021.2"
                        }
                    """.trimIndent()
                )
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            "help",
            dryRun = true
        ).assertThat().buildSuccessful()
    }
}
