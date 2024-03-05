package com.avito

import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project

class DetektPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply(BaseDetektPlugin::class.java)

            tasks.named("detekt", Detekt::class.java).configure {
                it.description = "Check kt and kts files in a module"
                it.setSource(
                    files(
                        project.layout.projectDirectory.dir("src"),
                        project.layout.projectDirectory.file("build.gradle.kts"),
                    )
                )
            }
        }
    }
}
