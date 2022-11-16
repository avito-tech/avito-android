package com.avito.android.tech_budget

import com.avito.android.tech_budget.internal.owners.OwnersConfigurator
import com.avito.android.tech_budget.internal.warnings.WarningsConfigurator
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class TechBudgetPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val configurators = setOf(
            WarningsConfigurator(),
            OwnersConfigurator(),
        )

        if (target.isRoot()) {
            target.extensions.create<TechBudgetExtension>("techBudget")
            configurators.forEach { it.configureUpload(target) }
        } else {
            if (target.getBooleanProperty("com.avito.android.tech-budget.enable", default = false)) {
                configurators.forEach { it.configureCollect(target) }
            }
        }
    }
}
