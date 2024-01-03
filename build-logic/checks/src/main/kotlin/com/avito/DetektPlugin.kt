package com.avito

import com.avito.android.withVersionCatalog
import io.gitlab.arturbosch.detekt.Detekt
import org.gradle.api.Plugin
import org.gradle.api.Project

class DetektPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        with(project) {
            plugins.apply("base")
            plugins.apply("io.gitlab.arturbosch.detekt")
            withVersionCatalog { libs ->
                project.dependencies.add("detektPlugins", libs.detektFormatting)
            }

            val detektTask = tasks.named("detekt", Detekt::class.java)
            detektTask.configure { task ->
                task.apply {
                    description = "Check kt and kts files in a module"
                    parallel = true
                    setSource(
                        files(
                            project.layout.projectDirectory.dir("src"),
                            project.layout.projectDirectory.file("build.gradle.kts"),
                        )
                    )

                    /**
                     * About config:
                     * yaml is a copy of https://github.com/detekt/detekt/blob/master/detekt-core/src/main/resources/default-detekt-config.yml
                     * all rules are disabled by default, enabled one by one
                     */
                    config.setFrom(files(project.rootDir.resolve("conf/detekt.yml")))
                    buildUponDefaultConfig = false

                    include("**/*.kt")
                    include("**/*.kts")
                    reports { report ->
                        report.xml.enabled = false
                        report.html.enabled = false
                    }
                }
            }

            tasks.named("check").configure { check ->
                check.dependsOn(detektTask)
            }
        }
    }
}
