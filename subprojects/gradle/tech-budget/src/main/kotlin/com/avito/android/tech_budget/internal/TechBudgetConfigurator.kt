package com.avito.android.tech_budget.internal

import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project

/**
 * Configurator used to add different technical budget metrics to a plugin.
 */
internal abstract class TechBudgetConfigurator {

    fun configureUpload(root: Project) {
        require(root.isRoot()) {
            "TechBudgetConfigurator.configureUpload upload must be called only on root project! "
        }
        doConfigureUpload(root)
    }

    fun configureCollect(subProject: Project) {
        require(!subProject.isRoot()) {
            "TechBudgetConfigurator.configureCollect upload must be called only on subproject!"
        }
        doConfigureCollect(subProject)
    }

    protected abstract fun doConfigureUpload(root: Project)

    protected abstract fun doConfigureCollect(subProject: Project)
}
