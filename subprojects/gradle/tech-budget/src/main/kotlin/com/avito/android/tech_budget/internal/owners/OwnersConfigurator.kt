package com.avito.android.tech_budget.internal.owners

import com.avito.android.diff.ReportCodeOwnershipExtension
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class OwnersConfigurator : TechBudgetConfigurator() {

    override fun doConfigureUpload(root: Project) {
        root.tasks.register<UploadOwnersTask>(UploadOwnersTask.NAME) {
            val codeOwnersExtension = root.extensions.findByType<ReportCodeOwnershipExtension>()
            val techBudgetExtension = root.extensions.getByType<TechBudgetExtension>()
            requireNotNull(codeOwnersExtension) {
                "You must apply `com.avito.android.code-ownership` to run uploadOwners task!"
            }
            val currentOwners = codeOwnersExtension.expectedOwnersProvider.get().get()
            owners.set(currentOwners)
            ownerSerializer.set(root.requireCodeOwnershipExtension().requireOwnersSerializer())
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
        }
    }

    override fun doConfigureCollect(subProject: Project) {
        // no-op. We can collect warnings inside root project.
    }
}
