package com.avito.android

import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.Plugin
import org.gradle.api.Project

public class ModuleTypesPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.extensions.create(
            "module",
            ModuleTypeExtension::class.java,
            ModuleType.IMPLEMENTATION
        )

        if (!project.getBooleanProperty("avito.moduleTypeValidationEnabled", false)) return

        val checkProjectDependenciesTypeTask =
            project.tasks.register(
                "checkProjectDependenciesType",
                CheckProjectDependenciesTypeTask::class.java
            ) { task ->
                task.group = "verification"
            }
        project.addPreBuildTasks(checkProjectDependenciesTypeTask)
    }
}
