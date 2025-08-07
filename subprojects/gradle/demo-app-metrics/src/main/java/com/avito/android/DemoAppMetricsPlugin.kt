package com.avito.android

import com.avito.android.clickstream.ClickStreamSenderService
import com.avito.android.module_graph.GenerateModuleGraphTask
import com.avito.android.module_graph.extractor.ModuleGraphInfoExtractorService
import com.avito.android.module_graph.models.GradleDependency
import com.avito.android.module_type.ModuleTypeExtension
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

public class DemoAppMetricsPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        if (project.isRoot()) {
            registerCountDemoAppTask(project)
            registerGenerateModuleGraphTask(project)
        } else {
            configureCountDemoAppTask(project)
            configureGenerateModuleGraphTask(project)
        }
    }

    private fun registerGenerateModuleGraphTask(project: Project) {
        project.tasks.register<GenerateModuleGraphTask>(GenerateModuleGraphTask.NAME) {
            outputFile.set(project.layout.buildDirectory.file("module-graph.json"))
            infoExtractorService.set(ModuleGraphInfoExtractorService.provideService(project))
        }
    }

    private fun configureGenerateModuleGraphTask(project: Project) {
        project.rootProject.tasks.withType(GenerateModuleGraphTask::class.java).configureEach {
            val moduleTypeExtension = project.extensions.getByType<ModuleTypeExtension>()
            val edges = project.configurations
                .flatMap { configuration ->
                    configuration.dependencies
                        .withType(ProjectDependency::class.java)
                        .mapNotNull { dependency ->
                            val type = GradleDependency.Type.entries.firstOrNull { type ->
                                type.typeName == configuration.name
                            } ?: return@mapNotNull null
                            GradleDependency(
                                from = project.path,
                                to = dependency.path,
                                type = type,
                            )
                        }
                }
            it.dependencies.addAll(edges)
            it.modulesToModuleTypes.put(project.path, moduleTypeExtension.type)
        }
    }

    private fun registerCountDemoAppTask(target: Project) {
        target.tasks.register<CountDemoAppsTask>("countDemoApps") {
            val clickStreamService = ClickStreamSenderService.provideClickStreamEventService(target)
            clickStreamSenderService.set(clickStreamService)
            usesService(clickStreamService)
        }
    }

    private fun configureCountDemoAppTask(target: Project) {
        val moduleTypeExtension = target.extensions.getByType<ModuleTypeExtension>()
        target.rootProject.tasks.withType(CountDemoAppsTask::class.java).configureEach {
            it.moduleTypes.add(moduleTypeExtension.type)
        }
    }
}
