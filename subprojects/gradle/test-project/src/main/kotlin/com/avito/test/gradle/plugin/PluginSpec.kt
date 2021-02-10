package com.avito.test.gradle.plugin

import com.avito.test.gradle.dependencies.GradleScriptCompatible

/**
 * Analogue of [org.gradle.plugin.use.PluginDependencySpec].
 * Use [plugin] or [PluginsSpec.id] to create an instance
 */
class PluginSpec(
    val id: String
) : GradleScriptCompatible {

    var version: String? = null
        private set
    var apply: Boolean = true
        private set

    fun version(version: String?): PluginSpec {
        this.version = version
        return this
    }

    fun apply(apply: Boolean): PluginSpec {
        this.apply = apply
        return this
    }

    override fun getScriptRepresentation(): String {
        return buildString {
            append("id \"$id\"")
            if (version != null) {
                append(" version \"$version\"")
            }
            if (!apply) {
                append(" apply false")
            }
        }
    }

    override fun toString(): String = getScriptRepresentation()
}

fun plugin(id: String): PluginSpec = PluginSpec(id)
