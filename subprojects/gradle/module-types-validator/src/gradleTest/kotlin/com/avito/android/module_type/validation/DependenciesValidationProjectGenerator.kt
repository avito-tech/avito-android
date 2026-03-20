package com.avito.android.module_type.validation

import com.avito.android.module_type.FunctionalType
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.module.FolderModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.PluginsSpec
import com.avito.test.gradle.plugin.plugins
import java.io.File

@Suppress("DEPRECATION")
internal object DependenciesValidationProjectGenerator {

    /**
     * Generated Project structure
     * ```
     * rootapp:
     *  - :lib-a
     *      - :public
     *      - :impl
     *      - :fake
     *      - :debug
     *  - :lib-b
     *      - :public
     *      - :impl
     *      - :fake
     *      - :demo
     *  - :lib-c
     *      - :impl
     *      - :demo
     *  - :heavy-module
     * ```
     *
     * Dependencies between generated modules
     * ```mermaid
     * graph LR
     * :lib-c:demo --> :lib-c:impl
     * :lib-c:demo --> :lib-b:fake
     * :lib-c:demo -.-> :lib-a:fake
     * :lib-c:demo -.-> :lib-a:debug
     * :lib-c:demo -.-> :lib-a:impl
     * :lib-c:impl --> :lib-b:public
     * :lib-b:public --> :lib-a:public
     * :lib-b:impl --> :lib-b:public
     * :lib-b:fake --> :lib-b:public
     * :lib-b:demo --> :lib-b:impl
     * :lib-b:demo -.-> :lib-a:fake
     * :lib-b:demo -.-> :lib-a:debug
     * :lib-b:demo -.-> :lib-a:impl
     * :lib-a:impl --> :lib-a:public
     * :lib-a:fake --> :lib-a:public
     * :lib-a:debug --> :lib-a:public
     * > :lib-c:demo --> :heavy-module is added only when [forbiddenDemoDependencyConnected] is `true`
     * ```
     *
     * > Use [mermaid](https://mermaid.live/edit#) for visualization
     */

    fun generateProject(
        projectDir: File,
        connectedFunctionalType: FunctionalType? = null,
        forbiddenDemoDependencyConnected: Boolean = false,
        allowForbiddenDemoDependency: Boolean = false,
        rootPlugins: PluginsSpec = plugins {
            id("com.avito.android.module-types")
            id("com.avito.android.module-types-validator")
        },
        implModulePlugins: PluginsSpec = plugins {
            id("com.avito.android.module-types")
        },
    ) {
        TestProjectGenerator(
            name = "rootapp",
            plugins = rootPlugins,
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
                            plugins = implModulePlugins
                        ),
                        createModule(
                            logicalModuleName = "lib-a",
                            functionalType = FunctionalType.Fake,
                            dependentModules = setOf(":lib-a:public")
                        ),
                        createModule(
                            logicalModuleName = "lib-a",
                            functionalType = FunctionalType.Debug,
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
                            plugins = implModulePlugins,
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
                        createDemoModule(
                            logicalModuleName = "lib-b",
                            dependentModules = setOfNotNull(
                                ":lib-b:impl",
                                connectedFunctionalType?.let { ":lib-a:${it.name.lowercase()}" }
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
                            plugins = implModulePlugins,
                            dependentModules = setOf(":lib-b:public")
                        ),
                        createDemoModule(
                            logicalModuleName = "lib-c",
                            dependentModules = setOfNotNull(
                                ":lib-c:impl",
                                ":lib-b:fake",
                                connectedFunctionalType?.let { ":lib-a:${it.name.lowercase()}" },
                                ":heavy-module".takeIf { forbiddenDemoDependencyConnected }
                            ),
                            allowForbiddenDemoDependency = allowForbiddenDemoDependency
                        ),
                    )
                ),
                createForbiddenModule()
            ),
        ).generateIn(projectDir)

        File(projectDir, "baselines")
            .mkdirs()
        File(projectDir, "baselines/forbidden-demo-dependencies.txt")
            .writeText(":heavy-module")
    }

    private fun createModule(
        logicalModuleName: String,
        functionalType: FunctionalType,
        dependentModules: Set<String> = emptySet(),
        plugins: PluginsSpec = plugins {
            id("com.avito.android.module-types")
        },
    ): KotlinModule {
        return KotlinModule(
            name = functionalType.name.lowercase(),
            packageName = logicalModuleName,
            imports = listOf("import com.avito.android.module_type.*"),
            plugins = plugins,
            buildGradleExtra = """
                module {
                    type = new ModuleType(new StubApplication(), FunctionalType.${functionalType.name})
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
        allowForbiddenDemoDependency: Boolean = false,
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
                    type = new ModuleType(
                        new StubApplication(), 
                        FunctionalType.${FunctionalType.DemoApp.name}
                    )
                    validation { 
                        missingImplementations {
                            configurationNames.set(SetsKt.setOf("implementation"))
                        }
                        forbiddenDemoDependencies {
                            forbiddenDependencies(file("${'$'}rootDir/baselines/forbidden-demo-dependencies.txt"))
                            ${if (allowForbiddenDemoDependency) "allow(\":heavy-module\")" else ""}
                        }
                    }
                }
            """.trimIndent(),
        )
    }

    private fun createForbiddenModule(): KotlinModule {
        return KotlinModule(
            name = "heavy-module",
            packageName = "heavy.module",
            imports = listOf("import com.avito.android.module_type.*"),
            plugins = plugins {
                id("com.avito.android.module-types")
            },
            buildGradleExtra = """
                module {
                    type = new ModuleType(new StubApplication(), FunctionalType.${FunctionalType.Util.name})
                }
            """.trimIndent(),
        )
    }
}
