package com.avito.test.gradle.module

import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.module
import java.io.File

class PlatformModule(
    override val name: String,
    override val plugins: List<String> = emptyList(),
    override val buildGradleExtra: String = ""
) : Module {

    override val modules: List<Module> = emptyList()
    override val dependencies: Set<GradleDependency> = emptySet()

    override fun generateIn(file: File) {
        val customPlugins = plugins.joinToString(separator = "\n") { "id '$it'" }

        file.module(name) {
            build_gradle {
                writeText(
                    """
plugins {
    id 'java-platform'
    $customPlugins
}

$buildGradleExtra
"""
                )
            }
        }
    }
}
