package com.avito.test.gradle.module

import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import java.io.File

/**
 * Empty module. It setups build.gradle for all child modules
 */
class ParentGradleModule(
    override val name: String,
    override val modules: List<Module>,
    override val plugins: PluginsSpec = PluginsSpec(),
    override val buildGradleExtra: String = ""
) : Module {

    override val dependencies: Set<GradleDependency> = emptySet()

    override fun generateIn(file: File) {
        file.module(name) {
            build_gradle {
                writeText(
                    "subprojects { " +
                        "afterEvaluate { " +
                        "println(\"\$name project configuration was altered by parent module's build.gradle\") " +
                        "}" +
                        "}"
                )
            }
            modules.forEach { it.generateIn(this) }
        }
    }
}
