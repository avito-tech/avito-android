package com.avito.android.tech_budget.internal.warnings

import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.android.tech_budget.internal.warnings.report.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.upload.UploadWarningsTask
import com.avito.android.tech_budget.warnings.CollectWarningsTask
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class WarningsConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (project.isRoot()) {
            configureUpload(project)
        } else {
            configureCollect(project)
        }
    }

    private fun configureUpload(root: Project) {
        val extension = root.extensions.getByType<TechBudgetExtension>()
        root.tasks.register<UploadWarningsTask>(UploadWarningsTask.NAME) {
            this.dumpInfoConfiguration.set(extension.dumpInfo)
            this.ownerSerializer.set(root.requireCodeOwnershipExtension().ownerSerializer)
            this.uploadWarningsBatchSize.set(extension.warnings.uploadWarningsBatchSize)
            this.uploadWarningsParallelRequestsCount.set(extension.warnings.uploadWarningsParallelRequestsCount)
            this.issuesFileParser.set(extension.warnings.issuesFileParser)
        }
    }

    private fun configureCollect(subProject: Project) {
        require(subProject.rootProject.plugins.hasPlugin("com.avito.android.tech-budget")) {
            "Plugin `com.avito.android.tech-budget` must be applied to the root project"
        }
        val extension = subProject.rootProject.extensions.getByType<TechBudgetExtension>()
        configureRootCollectWarnings(subProject.rootProject, subProject, extension)
    }

    private fun configureRootCollectWarnings(
        root: Project,
        subProject: Project,
        extension: TechBudgetExtension,
    ) {
        root.tasks.withType<UploadWarningsTask>().configureEach { uploadWarningsTask ->
            val collectWarningsTask = subProject.tasks
                .typedNamed<CollectWarningsTask>(extension.warnings.compileWarningsTaskName.get())

            uploadWarningsTask.inputReports.put(
                ProjectInfo.fromProject(subProject),
                collectWarningsTask.map { it.warnings }
            )
        }
    }
}
