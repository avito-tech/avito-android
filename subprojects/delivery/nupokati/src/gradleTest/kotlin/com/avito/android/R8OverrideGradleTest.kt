package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

/**
 * GradleTestingPlugin puts R8 from the version catalog ahead of AGP on the plugin under test classpath,
 * so TestKit builds shrink with the same R8 as real builds instead of the one bundled into AGP
 */
internal class R8OverrideGradleTest {

    @Test
    fun `release with minification - build - runs R8 from version catalog instead of AGP bundled one`(
        @TempDir projectDir: File
    ) {
        TestProjectGenerator(
            buildGradleExtra = """println("R8 version: " + com.android.tools.r8.Version.getVersionString())""",
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    useKts = true,
                    buildGradleExtra = """
                        |android {
                        |    buildTypes {
                        |        getByName("release") {
                        |            isMinifyEnabled = true
                        |        }
                        |    }
                        |}
                        |""".trimMargin()
                )
            )
        ).generateIn(projectDir)

        val result = gradlew(projectDir, ":app:minifyReleaseWithR8")

        result.assertThat().buildSuccessful()
        result.assertThat().tasksShouldBeTriggered(":app:minifyReleaseWithR8")
        result.assertThat().outputContains("R8 version: ${System.getProperty("r8Version")}")
    }
}
