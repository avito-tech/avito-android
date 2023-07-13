package com.avito.android.tech_budget.internal.owners.dependencies

import com.avito.android.info.ExportExternalDepsCodeOwners
import com.avito.android.info.ExportInternalDepsCodeOwners
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.android.tech_budget.internal.owners.requireOwnersSerializerProvider
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class DependenciesConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (!project.isRoot()) return // upload and collecting dependencies is made only within root project
        val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()

        project.tasks.register<UploadDependenciesTask>("uploadDependencies") {
            val codeOwnershipExtension = project.requireCodeOwnershipExtension()
            val exportExternalDepsTask =
                project.tasks.typedNamed<ExportExternalDepsCodeOwners>(ExportExternalDepsCodeOwners.NAME)
            val exportInternalDepsTask =
                project.tasks.typedNamed<ExportInternalDepsCodeOwners>(ExportInternalDepsCodeOwners.NAME)

            ownerSerializer.set(codeOwnershipExtension.requireOwnersSerializerProvider())
            dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            externalDependencies.set(exportExternalDepsTask.get().outputFile)
            internalDependencies.set(exportInternalDepsTask.get().outputFile)

            dependsOn(exportInternalDepsTask)
            dependsOn(exportExternalDepsTask)
        }
    }
}
