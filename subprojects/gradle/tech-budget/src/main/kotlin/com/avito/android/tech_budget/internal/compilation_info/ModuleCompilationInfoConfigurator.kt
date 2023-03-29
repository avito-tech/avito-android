package com.avito.android.tech_budget.internal.compilation_info

import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class ModuleCompilationInfoConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (!project.isRoot()) return

        project.tasks.register<UploadModulesCompilationInfoTask>(UploadModulesCompilationInfoTask.NAME) {
            val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()

            this.ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializer)
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            this.compilationTimeFile.set(techBudgetExtension.compilationTimeFile)
        }
    }
}
