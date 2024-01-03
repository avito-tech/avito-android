package com.avito

import com.dropbox.gradle.plugins.dependencyguard.DependencyGuardPluginExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class DependencyLockingBasePlugin : Plugin<Project> {

    abstract val configurationName: String

    final override fun apply(project: Project) {
        with(project) {
            plugins.apply("base")
            plugins.apply("com.dropbox.dependency-guard")
            extensions.getByType(DependencyGuardPluginExtension::class.java).configuration(configurationName)
            tasks.named("check").configure {
                it.dependsOn(tasks.named("dependencyGuard"))
            }
        }
    }
}
