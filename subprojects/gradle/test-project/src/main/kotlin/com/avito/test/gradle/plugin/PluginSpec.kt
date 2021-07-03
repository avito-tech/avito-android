package com.avito.test.gradle.plugin

import com.avito.test.gradle.dependencies.GradleScriptCompatible

/**
 * Analogue of [org.gradle.plugin.use.PluginDependencySpec].
 * Use [plugin] or [PluginsSpec.id] to create an instance
 */
public class PluginSpec(
    public val id: String
) : GradleScriptCompatible {

    public var version: String? = null
        private set
    public var apply: Boolean = true
        private set

    public fun version(version: String?): PluginSpec {
        this.version = version
        return this
    }

    public fun apply(apply: Boolean): PluginSpec {
        this.apply = apply
        return this
    }

    override fun getScriptRepresentation(): String {
        return buildString {
            append("id(\"$id\")")
            if (version != null) {
                append(" version(\"$version\")")
            }
            if (!apply) {
                append(" apply(false)")
            }
        }
    }

    override fun toString(): String = getScriptRepresentation()
}

public fun plugin(id: String): PluginSpec = PluginSpec(id)
