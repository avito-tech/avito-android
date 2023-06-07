package com.avito.module_api_extraction

import com.autonomousapps.tasks.SynthesizeProjectViewTask
import com.avito.android.module_type.ModuleTypeExtension
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.withType
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register

public class ModuleApiExtractionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        require(target.isRoot()) {
            "must be applied to the root project"
        }

        val extension = target.extensions.create<ModuleApiExtractionExtension>("moduleApiExtraction")

        target.tasks.register<ModuleApiExtractionTask>("extractApiFromModules") {
            require(project.plugins.hasPlugin("com.autonomousapps.dependency-analysis")) {
                "Dependency Analysis plugin should be applied "
            }

            val shouldModuleBeExamined = extension.shouldModuleBeExamined.get()

            val moduleToJsonFilePairs = target.subprojects.flatMap { subproject ->
                val type = subproject.extensions.findByType<ModuleTypeExtension>()?.type?.get()
                if (type == null || !shouldModuleBeExamined(type)) {
                    return@flatMap emptyList()
                }

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
