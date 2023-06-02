package com.avito.module_api_extraction

import com.autonomousapps.tasks.SynthesizeProjectViewTask
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.withType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

public class ModuleApiExtractionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.isRoot()) {
            "must be applied to the root project"
        }

        target.tasks.register<ModuleApiExtractionTask>("extractApiFromModules") {
            require(project.plugins.hasPlugin("com.autonomousapps.dependency-analysis")) {
                "Dependency Analysis plugin should be applied "
            }

            val moduleToJsonFilePairs = target.subprojects.flatMap { subproject ->
                subproject.tasks.withType<SynthesizeProjectViewTask>().map {
                    subproject.path to it.output
                }
            }

            moduleNames.set(moduleToJsonFilePairs.map { it.first })
            syntheticProjectJsonFiles.setFrom(moduleToJsonFilePairs.map { it.second })

            outputDir.set(
                project.layout.buildDirectory.dir(
                    "reports/module-api-extraction/"
                )
            )
        }
    }
}
