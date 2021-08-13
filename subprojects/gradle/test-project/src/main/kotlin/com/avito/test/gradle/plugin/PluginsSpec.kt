package com.avito.test.gradle.plugin

import com.avito.test.gradle.dependencies.GradleScriptCompatible

/**
 * Analogue of [org.gradle.plugin.use.PluginDependenciesSpec]
 * Use [plugins] to create an instance
 */
public class PluginsSpec internal constructor(
    private val plugins: MutableList<PluginSpec> = mutableListOf(),
    private val classpathPlugins: MutableList<ClasspathPluginSpec> = mutableListOf()
) : GradleScriptCompatible, ClasspathPluginsSpecDsl, PluginsSpecDsl {

    public override fun classpathPlugin(
        pluginClasspath: String,
        pluginId: String
    ) {
        classpathPlugins.add(
            ClasspathPluginSpec(
                ClasspathPluginSpec.ClasspathArtifact(pluginClasspath),
                ClasspathPluginSpec.PluginArtifact(pluginId)
            )
        )
    }

    override fun id(id: String): PluginSpec {
        val spec = PluginSpec(id)
        plugins.add(spec)
        return spec
    }

    override fun getScriptRepresentation(): String {
        return """
            |${classPaths()}
            |${pluginIds()}
            |${applyPlugins()}
        """.trimMargin().trimBlankLines()
    }

    private fun classPaths(): String =
        if (classpathPlugins.isEmpty()) {
            ""
        } else {
            val dependencies = classpathPlugins.map { it.classpathArtifact }
                .distinctBy { it.getScriptRepresentation() }
                .joinToString(separator = "\n")
            """
                |buildscript {
                |   dependencies {
                |       $dependencies
                |   }
                |}
            """
        }

    private fun applyPlugins(): String {
        return if (classpathPlugins.isEmpty()) {
            ""
        } else {
            val applies = classpathPlugins.map { it.pluginArtifact }
                .joinToString(separator = "\n")
            """
            |apply {
            |   $applies
            |}
            """.trimMargin()
        }
    }

    private fun pluginIds(): String {
        return if (plugins.isEmpty()) {
            ""
        } else {
            val pluginIds = plugins.joinToString(separator = "\n    ")
            """
            |plugins {
            |    $pluginIds
            |}
            """.trimMargin()
        }
    }

    private fun String.trimBlankLines(): String {
        val lines = lines()
        return lines
            .filter(String::isNotBlank)
            .joinToString(separator = "\n")
    }

    override fun toString(): String = getScriptRepresentation()

    internal fun plus(other: PluginsSpec): PluginsSpec {
        return PluginsSpec(
            plugins = (this.plugins + other.plugins).toMutableList(),
            classpathPlugins = (this.classpathPlugins + other.classpathPlugins).toMutableList()
        )
    }
}

public fun plugins(init: PluginsSpec.() -> Unit = {}): PluginsSpec {
    val plugins = PluginsSpec()
    plugins.init()
    return plugins
}
