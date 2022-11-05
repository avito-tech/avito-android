package com.avito.android.tech_budget

import com.avito.android.tech_budget.internal.warnings.WarningsConfigurator
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project

public class TechBudgetPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val configurators = setOf(
            WarningsConfigurator(),
        )

        if (target.isRoot()) {
            configurators.forEach { it.configureUpload(target) }
        } else {
            if (target.getBooleanProperty("com.avito.android.tech-budget.enable", default = false)) {
                configurators.forEach { it.configureCollect(target) }
            } else {
                target.logger.lifecycle(
                    "Tech budget collection disabled. " +
                        "You can enable by passing parameter `-Pcom.avito.android.tech-budget.enable=true`."
                )
            }
        }
    }
}
