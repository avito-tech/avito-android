package com.avito.test.gradle.plugin

import com.avito.test.gradle.dependencies.GradleScriptCompatible

/**
 * Analogue of [org.gradle.plugin.use.PluginDependenciesSpec]
 * Use [plugins] to create an instance
 */
class PluginsSpec(
    private val plugins: MutableList<PluginSpec> = mutableListOf()
) : GradleScriptCompatible {

    override fun getScriptRepresentation(): String {
        return """|plugins {
                  |    ${plugins.joinToString(separator = "\n    ") { it.getScriptRepresentation() }}
                  |}""".trimMargin()
    }

    override fun toString(): String = getScriptRepresentation()

    fun id(id: String): PluginSpec {
        val spec = PluginSpec(id)
        plugins.add(spec)
        return spec
    }

    fun plus(other: PluginsSpec): PluginsSpec {
        return PluginsSpec((this.plugins + other.plugins).toMutableList())
    }
}

fun plugins(init: PluginsSpec.() -> Unit = {}): PluginsSpec {
    val plugins = PluginsSpec()
    plugins.init()
    return plugins
}
