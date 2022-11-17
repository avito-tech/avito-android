package com.avito.android.tech_budget.internal

import org.gradle.api.Project

/**
 * Configurator used to add different technical budget metrics to a plugin.
 */
internal interface TechBudgetConfigurator {

    fun configure(project: Project)
}
