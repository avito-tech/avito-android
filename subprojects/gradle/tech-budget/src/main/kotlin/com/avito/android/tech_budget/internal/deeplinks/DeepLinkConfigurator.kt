package com.avito.android.tech_budget.internal.deeplinks

import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.deeplinks.CollectProjectDeeplinksTask
import com.avito.android.tech_budget.deeplinks.DeepLink
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.android.tech_budget.internal.utils.parser.JsonFileParser
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

            this.ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializersProvider)
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            this.deeplinksInput.set(collectProjectDeepLinksTask.flatMap { it.deeplinksOutput })
            this.deeplinksFileParser.set(
                techBudgetExtension.deepLinks.deepLinksFileParser
                    .orElse(JsonFileParser(ownerSerializer.get(), DeepLink::class))
            )
        }
    }
}
