package com.avito.android.module_type

import StubModuleType
import com.avito.module.configurations.ConfigurationType
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency.Safe.CONFIGURATION
import com.avito.test.gradle.dependencies.GradleDependency.Safe.Companion.project
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

/**
 * Generates the simplest project to check forbidden dependencies.
 * The structure: module --> dependency
 */
internal class ModuleTypesProjectGenerator(
    private val severity: Severity?,
    private val moduleType: StubModuleType,
    private val dependency: Dependency,
    private val constraint: Constraint,
    private val exclusions: Set<String>
) {

    class Dependency(
        val type: StubModuleType,
        val configuration: CONFIGURATION = CONFIGURATION.IMPLEMENTATION,
    )

    class Constraint(
        val module: StubModuleType,
        val dependency: StubModuleType,
        val configuration: ConfigurationType = ConfigurationType.Main,
    )

    private val Any.classReference: String
        get() = this::class.java.canonicalName

    fun generateIn(projectDir: File) {
        val severityDeclaration = if (severity != null) {
            "severity.set(${severity.classReference}.${severity.name})"
        } else {
            ""
        }
        val exclusionsDeclaration = exclusions.joinToString {
            "DependentModule(\"$it\")"
        }

        TestProjectGenerator(
            imports = listOf("import com.avito.android.module_type.*"),
            plugins = plugins {
                id(pluginId)
            },
            modules = listOf(
                KotlinModule(
                    "A",
                    imports = listOf("import com.avito.android.module_type.*"),
                    plugins = plugins {
                        id(pluginId)
                    },
                    dependencies = setOf(
                        project(":B", dependency.configuration)
                    ),
                    buildGradleExtra = """
                        module {
                            type.set(${moduleType.classReference})
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
                            type.set(${dependency.type.classReference})
                        }
                        """.trimIndent(),
                    useKts = true
                )
            ),
            buildGradleExtra = """
                moduleTypes {
                    $severityDeclaration
                    
                    restrictions.add(
                        DependencyRestriction(
                            matcher = BetweenModuleTypes(
                                module = ${constraint.module.classReference}, 
                                dependency = ${constraint.dependency.classReference},
                                configuration = ${constraint.configuration.classReference}
                            ), 
                            exclusions = setOf($exclusionsDeclaration),
                        )
                    )
                }
            """.trimIndent(),
            useKts = true
        ).generateIn(projectDir)
    }
}
