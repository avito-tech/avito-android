package com.avito.test.gradle.module

import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import com.avito.test.gradle.plugin.plugins
import java.io.File

class PlatformModule(
    override val name: String,
    override val plugins: PluginsSpec = plugins(),
    override val buildGradleExtra: String = ""
) : Module {

    override val modules: List<Module> = emptyList()
    override val dependencies: Set<GradleDependency> = emptySet()

    override fun generateIn(file: File) {
        val plugins = plugins {
            id("java-platform")
        }
            .plus(plugins)

        file.module(name) {
            build_gradle {
                writeText(
                    """
${plugins.getScriptRepresentation()}

$buildGradleExtra
"""
                )
            }
        }
    }
}
