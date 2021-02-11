package com.avito.test.gradle.module

import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import java.io.File

/**
 * The folder for modules
 * :test:utils <- for example
 */
class FolderModule(
    override val name: String,
    override val modules: List<Module>
) : Module {
    override val dependencies: Set<GradleDependency> = emptySet()
    override val plugins: PluginsSpec = PluginsSpec()
    override val buildGradleExtra = ""

    override fun generateIn(file: File) {
        file.module(name) {
            modules.forEach { it.generateIn(this) }
        }
    }
}
