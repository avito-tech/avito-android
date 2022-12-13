package com.avito.android.tech_budget.internal.feature_toggles

import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.feature_toggles.CollectProjectFeatureTogglesTask
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.feature_toggles.serializer.JsonFeatureToggleFileParser
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class FeatureTogglesConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        project.tasks.register<UploadFeatureTogglesTask>(UploadFeatureTogglesTask.NAME) {
            val techBudgetExtension = project.extensions.getByType<TechBudgetExtension>()
            val collectProjectFeatureTogglesTask = project.tasks.typedNamed<CollectProjectFeatureTogglesTask>(
                techBudgetExtension.featureToggles.collectProjectFeatureTogglesTaskName.get()
            )

            dependsOn(collectProjectFeatureTogglesTask)
            this.ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializer)
            this.dumpInfoConfiguration.set(techBudgetExtension.dumpInfo)
            this.featureTogglesInput.set(collectProjectFeatureTogglesTask.get().featureTogglesOutput)
            this.featureTogglesFileParser.set(
                techBudgetExtension.featureToggles.featureTogglesFileParser
                    .orElse(JsonFeatureToggleFileParser(ownerSerializer.get()))
            )
        }
    }
}
