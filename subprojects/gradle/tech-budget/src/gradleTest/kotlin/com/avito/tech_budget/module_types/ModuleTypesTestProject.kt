package com.avito.tech_budget.module_types

import com.avito.android.utils.FAKE_OWNERSHIP_EXTENSION
import com.avito.tech_budget.utils.dumpInfoExtension
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

internal object ModuleTypesTestProject {

    fun generate(dir: File, mockWebServerUrl: String) {
        TestProjectGenerator(
            imports = listOf("import com.avito.tech_budget.module_types.*"),
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.code-ownership")
                id("com.avito.android.tech-budget")
                id("com.avito.android.module-types")
            },
            useKts = true,
            buildGradleExtra = """
                $FAKE_OWNERSHIP_EXTENSION
                techBudget {
                    ${dumpInfoExtension(mockWebServerUrl)}
                    
                    getModuleFunctionalTypeName.set {
                        (it as StubModuleType).type.name
                    }
                }
            """.trimIndent(),
            modules = listOf(
                KotlinModule(
                    name = "A",
                    imports = listOf("import com.avito.tech_budget.module_types.*"),
                    plugins = plugins {
                        id("com.avito.android.module-types")
                    },
                    buildGradleExtra = """
                        module {
                            type.set(StubModuleType(FunctionalType.Library))
                        }
                    """.trimIndent(),
                    useKts = true
                ),
                KotlinModule(
                    name = "B",
                    imports = listOf("import com.avito.tech_budget.module_types.*"),
                    plugins = plugins {
                        id("com.avito.android.module-types")
                    },
                    buildGradleExtra = """
                        module {
                            type.set(StubModuleType(FunctionalType.Public))
                        }
                    """.trimIndent(),
                    useKts = true
                ),
                KotlinModule(
                    name = "C",
                    imports = listOf("import com.avito.tech_budget.module_types.*"),
                    plugins = plugins {
                        id("com.avito.android.module-types")
                    },
                    buildGradleExtra = """
                        module {
                            type.set(StubModuleType(FunctionalType.Impl))
                        }
                    """.trimIndent(),
                    useKts = true
                ),
            )
        )
            .generateIn(dir)
    }
}
