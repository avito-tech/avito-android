package com.avito

import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.named

class DetektRootPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply(BaseDetektPlugin::class.java)

            tasks.named<Detekt>("detekt").configure {
                it.description = "Check build-logic kt and kts files"
                it.setSource(
                    layout.projectDirectory.dir("build-logic"),
                )
                it.exclude("**/resources/**")
                it.exclude("**/build/**")
            }
        }
    }
}
