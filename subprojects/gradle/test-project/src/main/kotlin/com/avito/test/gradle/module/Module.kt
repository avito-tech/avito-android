package com.avito.test.gradle.module

import com.avito.test.gradle.Generator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.plugin.PluginsSpec

public interface Module : Generator {
    public val name: String
    public val imports: List<String>
    public val plugins: PluginsSpec
    public val buildGradleExtra: String
    public val dependencies: Set<GradleDependency>
    public val modules: List<Module>
    public val useKts: Boolean
}

internal fun Module.imports(): String =
    imports.joinToString(separator = "\n")
