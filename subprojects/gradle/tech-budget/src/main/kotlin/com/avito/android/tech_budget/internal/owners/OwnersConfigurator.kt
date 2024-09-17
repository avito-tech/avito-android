package com.avito.android.tech_budget.internal.owners

import com.avito.android.diff.ReportCodeOwnershipExtension
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.service.usesRetrofitBuilderService
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class OwnersConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (!project.isRoot()) return // upload and collecting owners is made only within root project

        project.tasks.register<UploadOwnersTask>(UploadOwnersTask.NAME) {
            val codeOwnersExtension = project.extensions.findByType<ReportCodeOwnershipExtension>()
            val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()
            requireNotNull(codeOwnersExtension) {
                "You must apply `com.avito.android.code-ownership` to run uploadOwners task!"
            }
            val currentOwners = codeOwnersExtension.expectedOwnersProvider.get().get()
            owners.set(currentOwners)
            ownerSerializer.set(project.requireCodeOwnershipExtension().requireOwnersSerializerProvider())
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            this.techBudgetOwnerMapper.set(techBudgetExtension.owners.techBudgetOwnerMapper)

            usesRetrofitBuilderService(this.retrofitBuilderService)
        }
    }
}
