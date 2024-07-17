package com.avito.android.tech_budget.internal.perf_screen_owners

import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.android.tech_budget.internal.utils.parser.JsonFileParser
import com.avito.android.tech_budget.perf_screen_owners.CollectPerfOwnersTask
import com.avito.android.tech_budget.perf_screen_owners.PerformanceScreenInfo
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class PerfScreenOwnersConfigurator : TechBudgetConfigurator {
    override fun configure(project: Project) {
        if (!project.isRoot()) return // Collection is enabled only for root

        project.tasks.register<UploadPerfScreenOwnersTask>(UploadPerfScreenOwnersTask.NAME) {
            val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()
            val collectPerfOwnersTask = project.tasks.typedNamed<CollectPerfOwnersTask>(
                techBudgetExtension.perfOwners.collectProjectPerfOwnersTaskName.get()
            )

            this.ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializersProvider)
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            this.perfOwnersInput.set(collectPerfOwnersTask.flatMap { it.perfOwnersOutput })
            val defaultJsonFileParser = JsonFileParser(
                OwnerAdapterFactory(ownerSerializer.get().provideIdSerializer()),
                PerformanceScreenInfo::class
            )

            this.perfOwnersFileParser.set(
                techBudgetExtension.perfOwners.screenInfoFileParser
                    .orElse(defaultJsonFileParser)
            )
        }
    }
}
