package com.avito.android

import Visibility
import com.avito.android.CodeOwnershipValidationTest.Case.NegativeCase
import com.avito.android.CodeOwnershipValidationTest.Case.PositiveCase
import com.avito.test.gradle.ManualTempFolder
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.gradlew
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.plugin.plugins
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory

@Suppress("MaxLineLength")
class CodeOwnershipValidationTest {

    @TestFactory
    fun cases(): List<DynamicTest> = listOf(
        PositiveCase(
            featureOwnership = CodeOwnershipExtension("speed", Visibility.PUBLIC),
            featureModuleType = ModuleType.COMPONENT_TEST,
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.PRIVATE)
        ),
        NegativeCase(
            featureOwnership = CodeOwnershipExtension("performance", Visibility.TEAM),
            featureModuleType = ModuleType.COMPONENT_TEST,
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.TEAM),
            errorText = ":feature has forbidden dependency on :dependent_test_module"
        ),
        PositiveCase(
            featureOwnership = CodeOwnershipExtension("speed", Visibility.PRIVATE),
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.PUBLIC)
        ),
        NegativeCase(
            featureOwnership = CodeOwnershipExtension("speed", Visibility.PUBLIC),
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.PRIVATE),
            errorText = ":feature has forbidden dependency on :dependent_test_module"
        ),
        NegativeCase(
            featureOwnership = CodeOwnershipExtension("speed", Visibility.PUBLIC),
            featureModuleType = ModuleType.TEST_LIB,
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.PRIVATE),
            errorText = ""
        ),
        PositiveCase(
            featureOwnership = CodeOwnershipExtension("speed", Visibility.TEAM),
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.TEAM)
        ),
        NegativeCase(
            featureOwnership = CodeOwnershipExtension("performance", Visibility.TEAM),
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.TEAM),
            errorText = ":feature has forbidden dependency on :dependent_test_module"
        ),
        NegativeCase(
            featureOwnership = CodeOwnershipExtension("performance", Visibility.PRIVATE),
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.TEAM),
            errorText = ":feature has forbidden dependency on :dependent_test_module"
        ),
        PositiveCase(
            featureOwnership = CodeOwnershipExtension(
                "performance",
                Visibility.PRIVATE,
                allowedDependencies = setOf(":dependent_test_module")
            ),
            dependentOwnership = CodeOwnershipExtension("speed", Visibility.TEAM, emptySet())
        )
    ).map { case ->
        dynamicTest(case.displayName()) {

            ManualTempFolder.runIn { projectDir ->

                val featureAllowedDependencies = case.featureOwnership.allowedDependencies.joinToString(
                    separator = " ,",
                    prefix = "[",
                    postfix = "]",
                    transform = { "'$it'" }
                )

                val dependentAllowedDependencies = case.dependentOwnership.allowedDependencies.joinToString(
                    separator = " ,",
                    prefix = "[",
                    postfix = "]",
                    transform = { "'$it'" }
                )

                TestProjectGenerator(
                    plugins = plugins {
                        id("com.avito.android.impact")
                        id("com.avito.android.code-ownership")
                    },
                    modules = listOf(
                        AndroidAppModule(
                            "app",
                            plugins = plugins {
                                id("com.avito.android.module-types")
                            },
                            dependencies = setOf(
                                project(
                                    path = ":feature",
                                    configuration = ANDROID_TEST_IMPLEMENTATION
                                )
                            )
                        ),
                        AndroidLibModule(
                            "feature",
                            plugins = plugins {
                                id("com.avito.android.module-types")
                            },
                            dependencies = setOf(project(path = ":dependent_test_module")),
                            buildGradleExtra = """
                                ownership {
                                    team '${case.featureOwnership.team}.${case.featureOwnership.team}'
                                    visibility ${case.featureOwnership.visibility.declaringClass.name}.${case.featureOwnership.visibility.name}
                                    allowedDependencies = $featureAllowedDependencies
                                }
                            """.trimIndent().let {
                                if (case.featureModuleType != ModuleType.IMPLEMENTATION) {
                                    """
                                        $it

                                        module {
                                            type ${case.featureModuleType.declaringClass.name}.${case.featureModuleType.name}
                                        }
                                    """.trimIndent()
                                } else it
                            }
                        ),
                        AndroidLibModule(
                            "dependent_test_module",
                            plugins = plugins {
                                id("com.avito.android.module-types")
                            },
                            buildGradleExtra = """
                                ownership {
                                    team '${case.dependentOwnership.team}.${case.dependentOwnership.team}'
                                    visibility ${case.dependentOwnership.visibility.declaringClass.name}.${case.dependentOwnership.visibility.name}
                                    allowedDependencies = $dependentAllowedDependencies
                                }
                            """.trimIndent()
                        )
                    )
                ).generateIn(projectDir)

                when (case) {
                    is PositiveCase -> {
                        gradlew(
                            projectDir,
                            ":feature:checkProjectDependenciesOwnership",
                            "-Pavito.moduleOwnershipValidationEnabled=true",
                            "-PgitBranch=xxx" // todo need for impact plugin
                        ).assertThat().buildSuccessful()
                    }
                    is NegativeCase -> {
                        gradlew(
                            projectDir,
                            ":feature:checkProjectDependenciesOwnership",
                            "-Pavito.moduleOwnershipValidationEnabled=true",
                            "-PgitBranch=xxx", // todo need for impact plugin
                            expectFailure = true
                        ).assertThat().buildFailed(case.errorText)
                    }
                }
            }
        }
    }

    private sealed class Case(
        val featureOwnership: CodeOwnershipExtension,
        val featureModuleType: ModuleType,
        val dependentOwnership: CodeOwnershipExtension
    ) {

        class PositiveCase(
            featureOwnership: CodeOwnershipExtension,
            featureModuleType: ModuleType = ModuleType.IMPLEMENTATION,
            dependentOwnership: CodeOwnershipExtension
        ) : Case(featureOwnership, featureModuleType, dependentOwnership)

        class NegativeCase(
            featureOwnership: CodeOwnershipExtension,
            featureModuleType: ModuleType = ModuleType.IMPLEMENTATION,
            dependentOwnership: CodeOwnershipExtension,
            val errorText: String
        ) : Case(featureOwnership, featureModuleType, dependentOwnership)

        fun displayName(): String {
            val name = StringBuilder()

            if (featureModuleType != ModuleType.IMPLEMENTATION) {
                name.append(featureModuleType.name.toLowerCase())
                name.append(" ")
            }

            val hasDifferentVisibilities = featureOwnership.visibility != dependentOwnership.visibility
            if (hasDifferentVisibilities || featureOwnership.visibility == Visibility.TEAM) {
                name.append(featureOwnership.visibility.name.toLowerCase())
                name.append(" ")
            }
            name.append("module has ")
            if (hasDifferentVisibilities) {
                name.append(dependentOwnership.visibility.name.toLowerCase())
                name.append(" ")
            }
            val hasDifferentUnits = featureOwnership.team != dependentOwnership.team

            if (featureOwnership.visibility == Visibility.TEAM) {
                if (hasDifferentUnits) {
                    name.append("another unit")
                } else {
                    name.append("same unit")
                }
                name.append(" ")
            }
            name.append("dependency")
            name.append(" - ")
            if (this is PositiveCase) {
                name.append("build successful")
            } else {
                name.append("build failed with explanation")
            }

            return name.toString()
        }
    }
}
