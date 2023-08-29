package com.avito.android.module_type.validation

import com.avito.android.module_type.FunctionalType
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.module.FolderModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

internal object DependenciesValidationProjectGenerator {

    /**
     * Generated Project structure
     * ```
     * rootapp:
     *  - :lib-a
     *      - :public
     *      - :impl
     *      - :fake
     *  - :lib-b
     *      - :public
     *      - :impl
     *      - :fake
     *  - :lib-c
     *      - :impl
     *      - :demo
     * ```
     *
     * Dependencies between generated modules
     * ```mermaid
     * graph LR
     *  :lib-c:demo --> :lib-c:impl
     *  :lib-c:demo --> :lib-b:fake
     *
     *  :lib-c:demo -.-> :lib-a:fake
     *  :lib-c:demo -.-> :lib-a:impl
     *
     *  :lib-c:impl --> :lib-b:public
     *  :lib-b:public --> :lib-a:public
     *  :lib-b:impl --> :lib-b:public
     *  :lib-b:fake --> :lib-b:public
     *  :lib-a:impl --> :lib-a:public
     *  :lib-a:fake --> :lib-a:public
     * ```
     *
     * > Use [mermaid](https://mermaid-js.github.io/mermaid/#/) for visualization
     */

    fun generateProject(
        projectDir: File,
        connectedFunctionalType: FunctionalType? = null,
        applyPluginToImpl: Boolean = false
    ) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = plugins {
                id("com.avito.android.module-types")
                id("com.avito.android.module-types-validator")
            },
            modules = listOf(
                FolderModule(
                    name = "lib-a",
                    modules = listOf(
                        createModule(
                            logicalModuleName = "lib-a",
                            functionalType = FunctionalType.Public
                        ),
                        createModule(
                            logicalModuleName = "lib-a",
                            functionalType = FunctionalType.Impl,
                            dependentModules = setOf(":lib-a:public"),
                            applyPlugin = applyPluginToImpl
                        ),
                        createModule(
                            logicalModuleName = "lib-a",
                            functionalType = FunctionalType.Fake,
                            dependentModules = setOf(":lib-a:public")
                        ),
                    ),
                ),
                FolderModule(
                    name = "lib-b",
                    modules = listOf(
                        createModule(
                            logicalModuleName = "lib-b",
                            functionalType = FunctionalType.Public,
                            dependentModules = setOf(":lib-a:public"),
                        ),
                        createModule(
                            logicalModuleName = "lib-b",
                            functionalType = FunctionalType.Impl,
                            dependentModules = setOf(
                                ":lib-b:public"
                            )
                        ),
                        createModule(
                            logicalModuleName = "lib-b",
                            functionalType = FunctionalType.Fake,
                            dependentModules = setOf(
                                ":lib-b:impl",
                            )
                        ),
                    )
                ),
                FolderModule(
                    name = "lib-c",
                    modules = listOf(
                        createModule(
                            logicalModuleName = "lib-c",
                            functionalType = FunctionalType.Impl,
                            dependentModules = setOf(":lib-b:public")
                        ),
                        createDemoModule(
                            logicalModuleName = "lib-c",
                            dependentModules = setOfNotNull(
                                ":lib-c:impl",
                                ":lib-b:fake",
                                connectedFunctionalType?.let { ":lib-a:${it.name.lowercase()}" }
                            )
                        ),
                    )
                ),
            ),
        ).generateIn(projectDir)
    }

    private fun createModule(
        logicalModuleName: String,
        functionalType: FunctionalType,
        dependentModules: Set<String> = emptySet(),
        applyPlugin: Boolean = false
    ): KotlinModule {
        return KotlinModule(
            name = functionalType.name.lowercase(),
            packageName = logicalModuleName,
            imports = listOf("import com.avito.android.module_type.*"),
            plugins = plugins {
                id("com.avito.android.module-types")
                if (applyPlugin) {
                    id("com.avito.android.module-types-validator")
                }
            },
            buildGradleExtra = """
                module {
                    type = new DefaultModuleType(new StubApplication(), FunctionalType.${functionalType.name})
                }
            """.trimIndent(),
            dependencies = dependentModules.map {
                GradleDependency.Safe.project(
                    it,
                    GradleDependency.Safe.CONFIGURATION.API
                )
            }.toSet(),

            )
    }

    private fun createDemoModule(
        logicalModuleName: String,
        dependentModules: Set<String> = emptySet(),
    ): KotlinModule {
        return KotlinModule(
            name = "demo",
            packageName = "$logicalModuleName.demo",
            imports = listOf("import com.avito.android.module_type.*", "import kotlin.collections.SetsKt"),
            plugins = plugins {
                id("com.avito.android.module-types")
                id("com.avito.android.module-types-validator")
            },
            dependencies = dependentModules.map {
                GradleDependency.Safe.project(it)
            }
                .toSet(),
            buildGradleExtra = """
                module {
                    type = new DefaultModuleType(
                        new StubApplication(), 
                        FunctionalType.${FunctionalType.Application.name}
                    )
                    validation { 
                        publicImpl {
                            configurationNames.set(SetsKt.setOf("implementation"))
                        }
                    }
                }
            """.trimIndent(),
        )
    }
}
