package com.avito.android.build_checks.unique_app_res

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.dir
import com.avito.test.gradle.file
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class UniqueAppResourcesTest {

    private lateinit var projectDir: File

    @BeforeEach
    internal fun setUp(@TempDir projectDir: File) {
        this.projectDir = projectDir
    }

    @Test
    fun `success - unique resources`() {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = "com.app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b")
                    ),
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueAppResources {
                                enabled = true
                            }
                        }
                        """.trimIndent()
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib_a"),
                AndroidLibModule(name = "lib-b", packageName = "lib_b"),
            ),
        ).generateIn(projectDir)

        val build = runCheck()

        build.assertThat().buildSuccessful()
        build.assertThat().tasksShouldBeTriggered(":app:checkUniqueResources")
    }

    @Test
    fun `fail - duplicated string resource`() {
        val resFileCreator: File.() -> Unit = {
            val projectName = this.name
            dir("src/main/res/values") {
                file(
                    "strings.xml",
                    content = """
                    <resources>
                        <string name="title">$projectName</string>
                    </resources>
                    """.trimIndent()
                )
            }
        }
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = "com.app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b")
                    ),
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueAppResources {
                                enabled = true
                            }
                        }
                        """.trimIndent()
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib_a", mutator = resFileCreator),
                AndroidLibModule(name = "lib-b", packageName = "lib_b", mutator = resFileCreator),
            ),
        ).generateIn(projectDir)

        val build = runCheck(expectFailure = true)

        build.assertThat()
            .buildFailed()
            .outputContains("string 'title' in packages: [lib_a, lib_b]")
    }

    @Test
    fun `success - ignore duplicated resource type`() {
        val resFileCreator: File.() -> Unit = {
            val projectName = this.name
            dir("src/main/res/values") {
                file(
                    "strings.xml",
                    content = """
                    <resources>
                        <string name="title">$projectName</string>
                    </resources>
                """.trimIndent()
                )
            }
        }
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = "com.app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b")
                    ),
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueAppResources {
                                enabled = true
                                ignoredResourceTypes.add("string")
                            }
                        }
                    """.trimIndent()
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib_a", mutator = resFileCreator),
                AndroidLibModule(name = "lib-b", packageName = "lib_b", mutator = resFileCreator),
            ),
        ).generateIn(projectDir)

        val build = runCheck()

        build.assertThat().buildSuccessful()
        build.assertThat().tasksShouldBeTriggered(":app:checkUniqueResources")
    }

    @Test
    fun `success - ignore specific resources`() {
        val resFileCreator: File.() -> Unit = {
            val projectName = this.name
            dir("src/main/res/values") {
                file(
                    "strings.xml",
                    content = """
                    <resources>
                        <string name="title">$projectName</string>
                        <string name="hint">$projectName</string>
                    </resources>
                    """.trimIndent()
                )
            }
        }
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = "com.app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    dependencies = setOf(
                        project(":lib-a"),
                        project(":lib-b")
                    ),
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueAppResources {
                                enabled = true
                                ignoredResource("string", "title")
                                ignoredResource("string", "hint")
                            }
                        }
                        """.trimIndent()
                ),
                AndroidLibModule(name = "lib-a", packageName = "lib_a", mutator = resFileCreator),
                AndroidLibModule(name = "lib-b", packageName = "lib_b", mutator = resFileCreator),
            ),
        ).generateIn(projectDir)

        val build = runCheck()

        build.assertThat().buildSuccessful()
        build.assertThat().tasksShouldBeTriggered(":app:checkUniqueResources")
    }

    @Test
    fun `fail - invalid resource type in a config`() {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.impact")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "app",
                    packageName = "com.app",
                    plugins = plugins {
                        id("com.avito.android.build-checks")
                    },
                    buildGradleExtra = """
                        buildChecks {
                            enableByDefault = false
                            uniqueAppResources {
                                enabled = true
                                ignoredResourceTypes.add("INVALID_RES_TYPE")
                            }
                        }
                        """.trimIndent()
                ),
            ),
        ).generateIn(projectDir)

        val build = gradlew(
            projectDir,
            "help",
            "-PgitBranch=xxx", // todo need for impact plugin
            expectFailure = true
        )

        build.assertThat()
            .buildFailed()
            .outputContains(
                "Unknown resource type 'INVALID_RES_TYPE'. " +
                    "See available values in com.android.resources.ResourceType's name."
            )
    }

    private fun runCheck(expectFailure: Boolean = false) = gradlew(
        projectDir,
        "checkBuildEnvironment",
        "-PgitBranch=xxx", // todo need for impact plugin
        expectFailure = expectFailure
    )
}
