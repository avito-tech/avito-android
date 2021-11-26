package com.avito.android

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class ExportOwnershipInfoTest {

    @Test
    internal fun `ownership exporting to csv file - works correctly`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            modules = listOf(
                AndroidAppModule(
                    "app",
                    imports = listOf("import com.avito.android.model.Owner"),
                    dependencies = setOf(
                        project(
                            path = ":feature",
                            configuration = IMPLEMENTATION
                        ),
                        project(
                            path = ":common",
                            configuration = IMPLEMENTATION
                        )
                    ),
                    buildGradleExtra = """
                        |object Speed : Owner { 
                        |   override fun toString(): String = "Speed"
                        |}
                        |
                        |ownership {
                        |    owners(Speed)
                        |}
                    """.trimMargin(),
                    useKts = true,
                ),
                AndroidLibModule(
                    name = "feature",
                    imports = listOf("import com.avito.android.model.Owner"),
                    buildGradleExtra = """
                        |object Speed : Owner {
                        |   override fun toString(): String = "Speed"
                        |}
                        |object Performance : Owner { 
                        |   override fun toString(): String = "Performance"
                        |}
                        |
                        |ownership {
                        |    owners(Speed, Performance)
                        |}
                    """.trimMargin(),
                    useKts = true,
                ),
                KotlinModule(name = "common")
            )
        ).generateIn(projectDir)

        gradlew(
            projectDir,
            "exportCodeOwnershipInfo",
        ).assertThat().buildSuccessful()

        val file = File(projectDir, "ownership.csv")
        assertTrue(file.exists())

        assertEquals("""
            |:app,"Speed"
            |:common,""
            |:feature,"Speed,Performance"
            |
        """.trimMargin(), file.readText())
    }
}
