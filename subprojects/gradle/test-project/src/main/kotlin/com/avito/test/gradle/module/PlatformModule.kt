package com.avito.test.gradle.module

import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.files.build_gradle
import com.avito.test.gradle.files.build_gradle_kts
import com.avito.test.gradle.module
import com.avito.test.gradle.plugin.PluginsSpec
import com.avito.test.gradle.plugin.plugins
import java.io.File

public class PlatformModule(
    override val name: String,
    override val plugins: PluginsSpec = plugins(),
    override val buildGradleExtra: String = "",
    override val useKts: Boolean = false,
) : Module {

    override val imports: List<String> = emptyList()
    override val modules: List<Module> = emptyList()
    override val dependencies: Set<GradleDependency> = emptySet()

    override fun generateIn(file: File) {
        val plugins = plugins {
            id("java-platform")
        }
            .plus(plugins)

        file.module(name) {

            val buildGradleContent = """
                |${plugins.getScriptRepresentation()}
                |
                |$buildGradleExtra
                """.trimMargin()

            if (useKts) {
                build_gradle_kts {
                    writeText(buildGradleContent)
                }
            } else {
                build_gradle {
                    writeText(buildGradleContent)
                }
            }
        }
    }
}
