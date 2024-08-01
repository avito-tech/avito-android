package com.avito.android.module_type

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

/**
 * Generates the simplest project to check forbidden dependencies.
 * The structure: module --> dependency
 */
internal class ModuleTypesProjectGenerator(
    private val config: ModuleTypesProjectConfig,
) {

    data class ModuleTypesProjectConfig(
        val defaultSeverity: Severity = Severity.fail,
    )

    private val Any.javaCanonicalName
        get() = this::class.java.canonicalName

    fun generateIn(projectDir: File) {
        TestProjectGenerator(
            imports = listOf(
                "import com.avito.android.module_type.*",
                "import com.avito.module.configurations.ConfigurationType",
            ),
            plugins = plugins {
                id(pluginId)
            },
            buildGradleExtra = """
                moduleTypes {
                    dependencyRestrictions {
                        defaultSeverity.set(${config.defaultSeverity.javaCanonicalName}.${config.defaultSeverity.name})
                        betweenFunctionalTypes(
                            fromType = FunctionalType.Library,
                            allowedTypes = mapOf(ConfigurationType.Main to setOf(FunctionalType.Public)),
                            reason = "Between betweenFunctionalTypes"
                        ) {
                            modulesExclusion(
                                module = ":A",
                                dependency = ":B",
                                reason = "Is a test"
                            )
                        }
                        betweenDifferentApps(
                            reason = "Because betweenDifferentApps",
                            commonApp = CommonApp
                        )
                        toWiring(
                            reason = "Because toWiring"
                        )
                    }
                }
            """.trimIndent(),
            modules = listOf(
                KotlinModule(
                    "A",
                    imports = listOf("import com.avito.android.module_type.*"),
                    plugins = plugins {
                        id(pluginId)
                    },
                    dependencies = setOf(
                        project(":B")
                    ),
                    buildGradleExtra = """
                        module {
                            type.set(ModuleType(StubApplication, FunctionalType.Library))
                        }
                        """.trimIndent(),
                    useKts = true
                ),
                KotlinModule(
                    "B",
                    imports = listOf("import com.avito.android.module_type.*"),
                    plugins = plugins {
                        id(pluginId)
                    },
                    buildGradleExtra = """
                        module {
                            type.set(ModuleType(StubApplication, FunctionalType.Library))
                        }
                        """.trimIndent(),
                    useKts = true
                ),
            ),
            useKts = true
        ).generateIn(projectDir)
    }
}
