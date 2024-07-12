package com.avito.android.module_type.validation.internal

import com.avito.android.module_type.ModuleTypeExtension
import com.avito.android.module_type.ModuleTypesPlugin
import com.avito.android.module_type.validation.ModuleTypeValidationPlugin.Companion.PROJECT_LIST_TASK_NAME
import com.avito.android.module_type.validation.ValidationExtension
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.diagnostics.ProjectReportTask
import org.gradle.kotlin.dsl.getByType
import java.io.File

internal fun Project.hasModuleTypePlugin(): Boolean {
    return plugins.hasPlugin(ModuleTypesPlugin::class.java)
}

internal fun Project.moduleTypeExtension(): ModuleTypeExtension {
    return extensions.getByType()
}

internal fun ModuleTypeExtension.validationExtension(): ValidationExtension {
    return extensions.getByType()
}

internal fun Project.projectListTaskOutput(): Provider<File> {
    val projectListTask = rootProject.tasks.typedNamed<ProjectReportTask>(PROJECT_LIST_TASK_NAME)
    return projectListTask.map { it.outputFile!! }
}
