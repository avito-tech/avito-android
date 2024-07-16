package com.avito.android.module_type.validation.configurations.missings.implementations

import com.avito.android.module_type.validation.configurations.ValidationConfiguration
import com.avito.android.module_type.validation.internal.projectListTaskOutput
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

internal class MissingFakeModuleConfiguration : ValidationConfiguration {

    override fun configureRoot(
        project: Project
    ) {
        val extension = project.extensions.create<MissingFakeModuleRootExtension>("missingFakeModule")

        project.tasks.register<MissingFakeModuleRootTask>(
            MissingFakeModuleRootTask.NAME
        ) {
            projectsTaskOutput.set(project.projectListTaskOutput())
            ignoreLogicalModulesRegexes.set(extension.ignoreLogicalModuleRegexes)
            outputFile.set(project.validationReportFile())
        }
    }

    override fun configureModule(
        project: Project,
    ) = Unit
}

private fun Project.validationReportFile(): Provider<RegularFile> {
    return layout.buildDirectory.file(
        "report/fake_module_validation.txt"
    )
}
