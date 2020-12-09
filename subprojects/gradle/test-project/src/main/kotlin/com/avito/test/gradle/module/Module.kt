package com.avito.test.gradle.module

import com.avito.test.gradle.Generator
import com.avito.test.gradle.dependencies.GradleDependency

interface Module : Generator {
    val name: String
    val plugins: List<String>
    val buildGradleExtra: String
    val dependencies: Set<GradleDependency>
    val modules: List<Module>
}
