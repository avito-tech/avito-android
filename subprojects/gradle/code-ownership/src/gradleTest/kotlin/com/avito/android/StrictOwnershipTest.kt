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

internal class StrictOwnershipTest {

    @TestFactory
    fun cases(): List<DynamicTest> = listOf(
        Case.PositiveCase(isOwnershipConfigured = true, isStrictOwnershipFlagEnabled = true),
        Case.PositiveCase(isOwnershipConfigured = true, isStrictOwnershipFlagEnabled = false),
        Case.NegativeCase(
            isOwnershipConfigured = false,
            isForceOwnershipFlagEnabled = true,
            errorMessage = "Owners must be set for the :app project."
        ),
        Case.PositiveCase(isOwnershipConfigured = false, isStrictOwnershipFlagEnabled = false),
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
                            dependencies = setOf(
                                project(
                                    path = ":feature",
                                    configuration = IMPLEMENTATION
                                )
                            ),
                            buildGradleExtra = if (case.isOwnershipConfigured) {
                                """
                                    |object Speed : Owner
                                    |
                                    |ownership {
                                    |    owners(Speed)
                                    |}
                                """.trimMargin()
                            } else "",
                            useKts = true,
                        ),
                        AndroidLibModule(
                            name = "feature",
                            imports = listOf("import com.avito.android.model.Owner"),
                            dependencies = setOf(
                                project(
                                    path = ":dependent_test_module",
                                    configuration = IMPLEMENTATION
                                )
                            ),
                            buildGradleExtra = if (case.isOwnershipConfigured) {
                                """
                                    |object Speed : Owner
                                    |object Performance : Owner
                                    |
                                    |ownership {
                                    |    owners(Speed, Performance)
                                    |}
                                """.trimMargin()
                            } else "",
                            useKts = true,
                        ),
                        AndroidLibModule(
                            name = "dependent_test_module",
                            imports = listOf("import com.avito.android.model.Owner"),
                            buildGradleExtra = if (case.isOwnershipConfigured) {
                                """
                                    |object MobileArchitecture : Owner
                                    |
                                    |ownership {
                                    |    owners(MobileArchitecture)
                                    |}
                                """.trimMargin()
                            } else "",
                            useKts = true,
                        )
                    )
                ).generateIn(projectDir)

                val args = mutableListOf(
                    "exportCodeOwnershipInfo",
                    "-PgitBranch=xxx"
                )
                if (case.isStrictOwnershipFlagEnabled) {
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
        val isStrictOwnershipFlagEnabled: Boolean
    ) {
        class PositiveCase(
            isOwnershipConfigured: Boolean,
            isStrictOwnershipFlagEnabled: Boolean,
        ) : Case(isOwnershipConfigured, isStrictOwnershipFlagEnabled)

        class NegativeCase(
            isOwnershipConfigured: Boolean,
            isForceOwnershipFlagEnabled: Boolean,
            val errorMessage: String
        ) : Case(isOwnershipConfigured, isForceOwnershipFlagEnabled)

        fun name(case: Case): String = buildString {
            when (isStrictOwnershipFlagEnabled) {
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
