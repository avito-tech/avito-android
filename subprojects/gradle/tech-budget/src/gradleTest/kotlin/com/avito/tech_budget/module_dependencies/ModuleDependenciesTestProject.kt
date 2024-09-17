package com.avito.tech_budget.module_dependencies

import com.avito.android.utils.FAKE_OWNERSHIP_EXTENSION
import com.avito.tech_budget.utils.dumpInfoExtension
import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

internal object ModuleDependenciesTestProject {

    /**
     * Scheme:
     * https://drive.google.com/file/d/1ElLG2jHV9sy-g5fHaHdLlBFMCCffoivg/view?usp=sharing
     */
    fun generate(dir: File, mockWebServerUrl: String) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.gradle-logger")
                id("com.avito.android.code-ownership")
                id("com.avito.android.tech-budget")
                id("com.avito.android.tls-configuration")
            },
            useKts = true,
            buildGradleExtra = """
                $FAKE_OWNERSHIP_EXTENSION
                techBudget {
                    ${dumpInfoExtension(mockWebServerUrl)}
                }
            """.trimIndent(),
            modules = listOf(
                AndroidAppModule(
                    name = "AppRootA",
                    dependencies = setOf(
                        GradleDependency.Safe.project(":NodeE"),
                        GradleDependency.Safe.project(":NodeF"),
                        GradleDependency.Safe.project(
                            ":NodeD",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        ),
                        GradleDependency.Safe.project(
                            ":NodeE",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        ),
                    )
                ),
                AndroidAppModule(
                    name = "AppRootB",
                    dependencies = setOf(
                        GradleDependency.Safe.project(":NodeF"),
                        GradleDependency.Raw("""stagingImplementation(project(":NodeF"))"""),
                        GradleDependency.Raw("""stagingImplementation(project(":NodeG"))"""),
                    )
                ),
                AndroidLibModule(name = "LibRootC", dependencies = setOf(GradleDependency.Safe.project(":NodeD"))),
                AndroidLibModule(name = "NodeD", dependencies = setOf(GradleDependency.Safe.project(":LeafH"))),
                KotlinModule(
                    name = "NodeE",
                    dependencies = setOf(
                        GradleDependency.Safe.project(
                            ":LeafJ",
                            GradleDependency.Safe.CONFIGURATION.TEST_IMPLEMENTATION
                        )
                    )
                ),
                AndroidLibModule(
                    name = "NodeF",
                    dependencies = setOf(
                        GradleDependency.Safe.project(":LeafK"),
                        GradleDependency.Safe.project(
                            ":LeafL",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        )
                    )
                ),
                KotlinModule(name = "NodeG", dependencies = setOf(GradleDependency.Safe.project(":LeafM"))),
                KotlinModule(name = "LeafH"),
                KotlinModule(name = "LeafJ"),
                KotlinModule(name = "LeafK"),
                KotlinModule(name = "LeafL"),
                KotlinModule(name = "LeafM"),
            )
        )
            .generateIn(dir)
    }
}
