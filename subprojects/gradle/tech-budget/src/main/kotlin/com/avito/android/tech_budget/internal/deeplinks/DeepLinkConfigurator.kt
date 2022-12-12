package com.avito.android.tech_budget.internal.deeplinks

import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.deeplinks.CollectProjectDeeplinksTask
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.deeplinks.serializer.JsonDeepLinksFileParser
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class DeepLinkConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (!project.isRoot()) return

        project.tasks.register<UploadDeepLinksTask>(UploadDeepLinksTask.NAME) {
            val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()
            val collectProjectDeepLinksTask = project.tasks.typedNamed<CollectProjectDeeplinksTask>(
                techBudgetExtension.deepLinks.collectProjectDeeplinksTaskName.get()
            )

            dependsOn(collectProjectDeepLinksTask)
            this.ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializer)
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            this.deeplinksInput.set(collectProjectDeepLinksTask.get().deeplinksOutput)
            this.deeplinksFileParser.set(
                techBudgetExtension.deepLinks.deepLinksFileParser
                    .orElse(JsonDeepLinksFileParser(ownerSerializer.get()))
            )
        }
    }
}
