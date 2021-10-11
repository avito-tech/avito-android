package com.avito.android

import com.avito.test.gradle.ManualTempFolder
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class ForceConfigurationTest {

    @TestFactory
    fun cases(): List<DynamicTest> = listOf(
        Case.PositiveCase(isOwnershipConfigured = true, isForceOwnershipFlagEnabled = true),
        Case.PositiveCase(isOwnershipConfigured = true, isForceOwnershipFlagEnabled = false),
        Case.NegativeCase(
            isOwnershipConfigured = false,
            isForceOwnershipFlagEnabled = true,
            errorMessage = "Owners must be set for :app"
        ),
        Case.PositiveCase(isOwnershipConfigured = false, isForceOwnershipFlagEnabled = false),
    ).map { case ->
        DynamicTest.dynamicTest(case.name(case)) {
            ManualTempFolder.runIn { projectDir ->
                TestProjectGenerator(
                    plugins = plugins {
                        id("com.avito.android.impact")
                        id("com.avito.android.code-ownership")
                    },
                    modules = listOf(
                        AndroidAppModule(
                            "app",
                            imports = listOf("import com.avito.android.model.Owner"),
                            plugins = plugins {
                                id("com.avito.android.module-types")
                            },
                            dependencies = setOf(
                                project(
                                    path = ":feature",
                                    configuration = IMPLEMENTATION
                                )
                            ),
                            buildGradleExtra = if (case.isOwnershipConfigured) {
                                """
                                    |def speed = new Owner() { }
                                    |
                                    |ownership {
                                    |    owners = [speed]
                                    |}
                                """.trimMargin()
                            } else ""
                        ),
                        AndroidLibModule(
                            name = "feature",
                            imports = listOf("import com.avito.android.model.Owner"),
                            plugins = plugins {
                                id("com.avito.android.module-types")
                            },
                            dependencies = setOf(
                                project(
                                    path = ":dependent_test_module",
                                    configuration = IMPLEMENTATION
                                )
                            ),
                            buildGradleExtra = if (case.isOwnershipConfigured) {
                                """
                                    |def speed = new Owner() { }
                                    |def performance = new Owner() { }
                                    |
                                    |ownership {
                                    |    owners = [speed, performance]
                                    |}
                                """.trimMargin()
                            } else ""
                        ),
                        AndroidLibModule(
                            name = "dependent_test_module",
                            imports = listOf("import com.avito.android.model.Owner"),
                            plugins = plugins {
                                id("com.avito.android.module-types")
                            },
                            buildGradleExtra = if (case.isOwnershipConfigured) {
                                """
                                    |def mobileArchitecture = new Owner() { }
                                    |
                                    |ownership {
                                    |    owners = [mobileArchitecture]
                                    |}
                                """.trimMargin()
                            } else ""
                        )
                    )
                ).generateIn(projectDir)

                val args = mutableListOf(
                    "exportCodeOwnershipInfo",
                    "-PgitBranch=xxx"
                )
                if (case.isForceOwnershipFlagEnabled) {
                    args.add("-Pavito.ownership.strictOwnership=true")
                }

                when (case) {
                    is Case.PositiveCase -> gradlew(
                        projectDir,
                        *args.toTypedArray()
                    ).assertThat().buildSuccessful()

                    is Case.NegativeCase -> gradlew(
                        projectDir,
                        *args.toTypedArray(),
                        expectFailure = true
                    ).assertThat().buildFailed().outputContains(case.errorMessage)
                }
            }
        }
    }

    private sealed class Case(
        val isOwnershipConfigured: Boolean,
        val isForceOwnershipFlagEnabled: Boolean
    ) {
        class PositiveCase(
            isOwnershipConfigured: Boolean,
            isForceOwnershipFlagEnabled: Boolean,
        ) : Case(isOwnershipConfigured, isForceOwnershipFlagEnabled)

        class NegativeCase(
            isOwnershipConfigured: Boolean,
            isForceOwnershipFlagEnabled: Boolean,
            val errorMessage: String
        ) : Case(isOwnershipConfigured, isForceOwnershipFlagEnabled)

        fun name(case: Case): String = buildString {
            when (isForceOwnershipFlagEnabled) {
                true -> append("force owners flag is enabled")
                false -> append("force owners flag is not enabled")
            }
            append(" - ")
            when (case) {
                is PositiveCase -> append("build succeeds")
                is NegativeCase -> append("build fails")
            }
            append(" - ")
            when (isOwnershipConfigured) {
                true -> append("when ownership is configured")
                false -> append("when ownership is not configured")
            }
        }
    }
}
