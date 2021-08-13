package com.avito.test.gradle.plugin

import com.avito.test.gradle.dependencies.GradleScriptCompatible

internal class ClasspathPluginSpec(
    internal val classpathArtifact: ClasspathArtifact,
    internal val pluginArtifact: PluginArtifact
) {
    internal class ClasspathArtifact(artifact: String) : GradleScriptCompatible {
        private val script: String = "classpath(\"$artifact\")"
        override fun getScriptRepresentation(): String = script
        override fun toString(): String = script
    }

    internal class PluginArtifact(artifact: String) : GradleScriptCompatible {
        private val script: String = "plugin(\"$artifact\")"
        override fun getScriptRepresentation(): String = script
        override fun toString(): String = script
    }
}
