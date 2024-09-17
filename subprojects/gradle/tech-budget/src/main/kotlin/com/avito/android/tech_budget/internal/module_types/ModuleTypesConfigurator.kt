package com.avito.android.tech_budget.internal.module_types

import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.android.tech_budget.internal.service.usesRetrofitBuilderService
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class ModuleTypesConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (!project.isRoot()) return

        project.tasks.register<UploadModuleTypesTask>(UploadModuleTypesTask.NAME) {
            val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()

            this.ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializersProvider)
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)

            usesRetrofitBuilderService(this.retrofitBuilderService)
        }
    }
}
