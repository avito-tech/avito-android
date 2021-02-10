package com.avito.test.gradle.module

import com.avito.test.gradle.Generator
import com.avito.test.gradle.dependencies.GradleDependency
import com.avito.test.gradle.plugin.PluginsSpec

interface Module : Generator {
    val name: String
    val plugins: PluginsSpec
    val buildGradleExtra: String
    val dependencies: Set<GradleDependency>
    val modules: List<Module>
}
