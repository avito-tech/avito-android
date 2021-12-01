package com.avito.module.dependencies

import com.avito.test.gradle.TestProjectGenerator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.module.AndroidAppModule
import com.avito.test.gradle.module.AndroidLibModule
import com.avito.test.gradle.module.KotlinModule
import com.avito.test.gradle.plugin.plugins
import java.io.File

internal object DependenciesGraphTestProject {

    fun generate(dir: File) {
        TestProjectGenerator(
            plugins = plugins {
                id("com.avito.android.module-dependencies-graph")
            },
            modules = listOf(
                AndroidAppModule(
                    name = "RootA",
                    dependencies = setOf(
                        GradleDependency.Safe.project(":LeafF"),
                        GradleDependency.Safe.project(":LeafE"),
                        GradleDependency.Safe.project(
                            ":LeafE",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        ),
                        GradleDependency.Safe.project(
                            ":UniqueA",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        ),
                    )
                ),
                AndroidAppModule(
                    name = "RootB",
                    dependencies = setOf(
                        GradleDependency.Safe.project(":NodeC"),
                        GradleDependency.Safe.project(
                            ":NodeG",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        ),
                        GradleDependency.Safe.project(
                            ":UniqueB",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        ),
                    )
                ),
                AndroidAppModule(
                    name = "CopyRootB",
                    dependencies = setOf(
                        GradleDependency.Safe.project(":NodeC"),
                        GradleDependency.Safe.project(
                            ":NodeG",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        ),
                        GradleDependency.Safe.project(
                            ":UniqueCopyB",
                            GradleDependency.Safe.CONFIGURATION.ANDROID_TEST_IMPLEMENTATION
                        ),
                    )
                ),
                AndroidLibModule(name = "NodeC", dependencies = setOf(GradleDependency.Safe.project(":NodeD"))),
                AndroidLibModule(name = "NodeG", dependencies = setOf(GradleDependency.Safe.project(":LeafE"))),
                KotlinModule(name = "NodeD", dependencies = setOf(GradleDependency.Safe.project(":LeafE"))),
                KotlinModule(name = "LeafE"),
                KotlinModule(name = "LeafF"),
                KotlinModule(name = "UniqueA"),
                KotlinModule(name = "UniqueB"),
                KotlinModule(name = "UniqueCopyB"),
                KotlinModule(name = "FakeRoot", dependencies = setOf(GradleDependency.Safe.project(":NodeC"))),
            )
        )
            .generateIn(dir)
    }
}
