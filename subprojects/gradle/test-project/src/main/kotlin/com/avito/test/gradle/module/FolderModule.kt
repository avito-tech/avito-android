package com.avito.test.gradle.module

import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import java.io.File

/**
 * The folder for modules
 * :test:utils <- for example
 */
public class FolderModule(
    override val name: String,
    override val modules: List<Module>
) : Module {
    override val imports: List<String> = emptyList()
    override val dependencies: Set<GradleDependency> = emptySet()
    override val plugins: PluginsSpec = PluginsSpec()
    override val buildGradleExtra: String = ""
    override val useKts: Boolean = false

    override fun generateIn(file: File) {
        file.module(name) {
            modules.forEach { it.generateIn(this) }
        }
    }
}
