package com.avito.android.tech_budget

import com.avito.android.tech_budget.internal.ab_tests.ABTestsConfigurator
import com.avito.android.tech_budget.internal.compilation_info.ModuleCompilationInfoConfigurator
import com.avito.android.tech_budget.internal.deeplinks.DeepLinkConfigurator
import com.avito.android.tech_budget.internal.detekt.DetektConfigurator
import com.avito.android.tech_budget.internal.feature_toggles.FeatureTogglesConfigurator
import com.avito.android.tech_budget.internal.lint_issues.LintIssuesConfigurator
import com.avito.android.tech_budget.internal.module_dependencies.ModuleDependenciesConfigurator
import com.avito.android.tech_budget.internal.module_types.ModuleTypesConfigurator
import com.avito.android.tech_budget.internal.owners.OwnersConfigurator
import com.avito.android.tech_budget.internal.owners.dependencies.DependenciesConfigurator
import com.avito.android.tech_budget.internal.perf_screen_owners.PerfScreenOwnersConfigurator
import com.avito.android.tech_budget.internal.warnings.WarningsConfigurator
import com.avito.kotlin.dsl.getBooleanProperty
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

public class TechBudgetPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        if (target.isRoot()) {
            target.extensions.create<TechBudgetExtension>("techBudget")
        }

        val isPluginEnabled = target.getBooleanProperty("com.avito.android.tech-budget.enable", default = false)
        if (!isPluginEnabled) return

        val configurators = setOf(
            OwnersConfigurator(),
            DependenciesConfigurator(),
            WarningsConfigurator(),
            DeepLinkConfigurator(),
            ABTestsConfigurator(),
            FeatureTogglesConfigurator(),
            LintIssuesConfigurator(),
            ModuleDependenciesConfigurator(),
            ModuleCompilationInfoConfigurator(),
            ModuleTypesConfigurator(),
            PerfScreenOwnersConfigurator(),
            DetektConfigurator(),
        )
        configurators.forEach { it.configure(target) }
    }
}
