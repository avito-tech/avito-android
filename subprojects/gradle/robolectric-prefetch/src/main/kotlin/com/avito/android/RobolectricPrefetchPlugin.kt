package com.avito.android

import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

@Suppress("UnstableApiUsage")
class RobolectricPrefetchPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.isRoot()) { "Plugin must be applied to root project but was applied to ${target.path}" }

        val extension = target.extensions.create<RobolectricPrefetchExtension>("robolectricPrefetch")

        target.afterEvaluate {
            extension.prefetchDependencies.get().forEach { (configuration, dependency) ->
                target.configurations.register(configuration)
                target.dependencies.add(configuration, dependency)
            }
        }

        target.tasks.register<RobolectricPrefetchTask>(prefetchTaskName) {
            group = "install"
            prefetchDependencies.set(extension.prefetchDependencies)
        }
    }
}
