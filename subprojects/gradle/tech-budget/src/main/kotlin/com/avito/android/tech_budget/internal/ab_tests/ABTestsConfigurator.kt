package com.avito.android.tech_budget.internal.ab_tests

import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.ab_tests.CollectProjectABTestsTask
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.ab_tests.serializer.JsonABTestFileParser
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class ABTestsConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        project.tasks.register<UploadABTestsTask>(UploadABTestsTask.NAME) {
            val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()
            val collectProjectABTestsTask = project.tasks.typedNamed<CollectProjectABTestsTask>(
                techBudgetExtension.abTests.collectProjectABTestsTaskName.get()
            )

            dependsOn(collectProjectABTestsTask)
            this.ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializer)
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            this.abTestsInput.set(collectProjectABTestsTask.get().abTestsOutput)
            this.abTestsFileParser.set(
                techBudgetExtension.abTests.abTestsFileParser
                    .orElse(JsonABTestFileParser(ownerSerializer.get()))
            )
        }
    }
}
