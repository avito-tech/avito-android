package com.avito.android.tech_budget.internal.ab_tests

import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.ab_tests.ABTest
import com.avito.android.tech_budget.ab_tests.CollectProjectABTestsTask
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.android.tech_budget.internal.utils.parser.JsonFileParser
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class ABTestsConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (!project.isRoot()) return // Collection is enabled only for root

        project.tasks.register<UploadABTestsTask>(UploadABTestsTask.NAME) {
            val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()
            val collectProjectABTestsTask = project.tasks.typedNamed<CollectProjectABTestsTask>(
                techBudgetExtension.abTests.collectProjectABTestsTaskName.get()
            )

            this.ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializersProvider)
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            this.abTestsInput.set(collectProjectABTestsTask.flatMap { it.abTestsOutput })
            val defaultJsonFileParser = JsonFileParser(
                OwnerAdapterFactory(ownerSerializer.get().provideIdSerializer()),
                ABTest::class
            )

            this.abTestsFileParser.set(
                techBudgetExtension.abTests.abTestsFileParser
                    .orElse(defaultJsonFileParser)
            )
        }
    }
}
