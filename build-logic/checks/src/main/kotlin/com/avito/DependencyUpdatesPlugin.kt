package com.avito

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class DependencyUpdatesPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("com.github.ben-manes.versions")
        target.tasks.withType(DependencyUpdatesTask::class.java).configureEach { task ->
            task.rejectVersionIf {
                isNonStable(it.candidate.version)
            }
        }
    }

    fun isNonStable(version: String): Boolean {
        val stableKeyword = listOf("RELEASE", "FINAL", "GA").any { version.toUpperCase().contains(it) }
        val regex = "^[0-9,.v-]+(-r)?$".toRegex()
        val isStable = stableKeyword || regex.matches(version)
        return isStable.not()
    }
}
