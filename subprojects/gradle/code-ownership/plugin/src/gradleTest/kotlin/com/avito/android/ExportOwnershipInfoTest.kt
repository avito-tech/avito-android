package com.avito.android

import com.avito.android.utils.FAKE_OWNERSHIP_EXTENSION
import com.avito.android.utils.LIBS_OWNERS_TOML_CONTENT
import com.avito.android.utils.LIBS_VERSIONS_TOML_CONTENT
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import java.io.File

internal class ExportOwnershipInfoTest {

    @Test
    internal fun `internal deps ownership exporting file - works correctly`(@TempDir projectDir: File) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            useKts = true,
            imports = listOf(
                "import com.avito.android.model.Owner",
                "import com.avito.android.serializers.OwnerIdSerializer",
                "import com.avito.android.serializers.OwnerNameSerializer",
                "import com.avito.android.OwnerSerializerProvider",
            ),
            buildGradleExtra = """
                        |object TestOwnerSerializersProvider : OwnerSerializerProvider {
                        |
                        |   override fun provideIdSerializer() = object : OwnerIdSerializer {
                        |       override fun deserialize(ownerName: String): com.avito.android.model.Owner {
                        |           error("Can't deserialize owner!")
                        |       }
                        |       
                        |       override fun serialize(owner: Owner): List<String> {
                        |           return listOf("Test" + owner.toString())
                        |       }
                        |   }
                        |   
                        |   override fun provideNameSerializer() = object : OwnerNameSerializer { 
                        |       override fun deserialize(ownerId: String): com.avito.android.model.Owner {
                        |           error("Can't deserialize owner!")
                        |       }
                        |       
                        |       override fun serialize(owner: Owner): String {
                        |           return "Test" + owner.toString()
                        |       }
                        |   }
                        |} 
                        |
                        |ownership {
                        |    ownerSerializersProvider.set(TestOwnerSerializersProvider)
                        |}
                    """.trimMargin(),
            modules = listOf(
                AndroidAppModule(
                    "app",
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                    },
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
                    plugins = plugins {
                        id("com.avito.android.code-ownership")
                    },
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
            "exportInternalDepsCodeOwners",
        ).assertThat().buildSuccessful()

        val file = File(projectDir, "build/ownership/internal-dependencies-owners.json")
        assertThat(file.exists()).isTrue()

        JSONAssert.assertEquals(file.readText(), EXPECTED_INTERNAL_DEPS_CODE_OWNERS, JSONCompareMode.LENIENT)
    }

    @Test
    internal fun `external deps ownership exporting file - works correctly`(@TempDir projectDir: File) {
        projectDir.file("gradle/libs.versions.toml", LIBS_VERSIONS_TOML_CONTENT)
        projectDir.file("gradle/libs.owners.toml", LIBS_OWNERS_TOML_CONTENT)
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            useKts = true,
            buildGradleExtra = FAKE_OWNERSHIP_EXTENSION

        ).generateIn(projectDir)

        runExportExternalDeps(projectDir)
            .assertThat()
            .buildSuccessful()

        val file = File(projectDir, "build/ownership/external-dependencies-owners.json")
        assertThat(file.exists()).isTrue()

        JSONAssert.assertEquals(file.readText(), EXPECTED_EXTERNAL_DEPS_CODE_OWNERS, JSONCompareMode.LENIENT)
    }

    @Test
    internal fun `external deps ownership exporting - remove output - build cache used`(@TempDir projectDir: File) {
        projectDir.file("gradle/libs.versions.toml", LIBS_VERSIONS_TOML_CONTENT)
        projectDir.file("gradle/libs.owners.toml", LIBS_OWNERS_TOML_CONTENT)
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.code-ownership")
            },
            useKts = true,
            buildGradleExtra = FAKE_OWNERSHIP_EXTENSION

        ).generateIn(projectDir)

        runExportExternalDeps(projectDir)
            .assertThat()
            .buildSuccessful()

        runExportExternalDeps(projectDir)
            .assertThat()
            .buildSuccessful()
            .taskWithOutcome(":exportExternalDepsCodeOwners", TaskOutcome.UP_TO_DATE)

        val output = File(projectDir, "build/ownership/external-dependencies-owners.json")
        output.delete()

        runExportExternalDeps(projectDir)
            .assertThat()
            .buildSuccessful()
            .taskWithOutcome(":exportExternalDepsCodeOwners", TaskOutcome.FROM_CACHE)
    }

    private fun runExportExternalDeps(projectDir: File) = gradlew(
        projectDir,
        "exportExternalDepsCodeOwners",
        "-Dorg.gradle.caching=true",
    )

    private companion object {

        @Language("json")
        private val EXPECTED_INTERNAL_DEPS_CODE_OWNERS = """
            [
               {
                  "moduleName":":app",
                  "owners":[
                     "TestSpeed"
                  ],
                  "type":"internal",
                  "betweennessCentrality": 0.0
               },
               {
                  "moduleName":":common",
                  "owners":[
                     
                  ],
                  "type":"internal",
                  "betweennessCentrality": 0.0
               },
               {
                  "moduleName":":feature",
                  "owners":[
                     "TestSpeed",
                     "TestPerformance"
                  ],
                  "type":"internal",
                  "betweennessCentrality": 0.0
               }
            ]
        """.trimIndent()

        @Language("json")
        private val EXPECTED_EXTERNAL_DEPS_CODE_OWNERS = """
            [
                {
                    "moduleName": "io.gitlab.arturbosch.detekt",
                    "owners": [
                        "Speed"
                    ],
                    "type": "external"
                },
                {
                    "moduleName": "com.google.code.gson:gson",
                    "owners": [
                        "Speed"
                    ],
                    "type": "external"
                },
                {
                    "moduleName": "androidx.core:core",
                    "owners": [
                        "Messenger"
                    ],
                    "type": "external"
                },
                {
                    "moduleName": "androidx.constraintlayout:constraintlayout",
                    "owners": [
                        "Messenger"
                    ],
                    "type": "external"
                }
            ]
            """
            .trimIndent()
    }
}
