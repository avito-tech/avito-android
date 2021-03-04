package com.avito.module.internal.dependencies

import org.gradle.api.Plugin
import org.gradle.api.Project

internal class TestPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            tasks.register("printDependenciesGraph", PrintDependenciesGraphTask::class.java)
        }
    }
}
