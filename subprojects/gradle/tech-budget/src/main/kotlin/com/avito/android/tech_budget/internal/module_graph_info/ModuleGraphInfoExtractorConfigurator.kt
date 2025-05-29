package com.avito.android.tech_budget.internal.module_graph_info

import com.avito.android.module_graph.GenerateModuleGraphTask
import com.avito.android.module_graph.ModuleGraphTask
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.module_graph_info.app_dependencies.UploadModuleGraphAppDependenciesTask
import com.avito.android.tech_budget.internal.module_graph_info.dependencies.UploadModuleGraphDependenciesTask
import com.avito.android.tech_budget.internal.module_graph_info.sizes.UploadModuleSizesTask
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.android.tech_budget.internal.service.usesRetrofitBuilderService
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamed
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class ModuleGraphInfoExtractorConfigurator : TechBudgetConfigurator {

    override fun configure(project: Project) {
        if (!project.isRoot()) return

        project.tasks.register<UploadModuleGraphAppDependenciesTask>(
            UploadModuleGraphAppDependenciesTask.NAME
        ) {
            val extension = project.extensions.getByType<TechBudgetExtension>()
            graphInfo.set(
                project.tasks.typedNamed<ModuleGraphTask>(GenerateModuleGraphTask.NAME)
                    .flatMap { it.outputFile }
            )
            ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializersProvider)
            dumpInfoConfiguration.set(extension.dumpInfo)
            usesRetrofitBuilderService(this.retrofitBuilderService)
        }

        project.tasks.register<UploadModuleGraphDependenciesTask>(UploadModuleGraphDependenciesTask.NAME) {
            val extension = project.extensions.getByType<TechBudgetExtension>()
            graphInfo.set(
                project.tasks.typedNamed<ModuleGraphTask>(GenerateModuleGraphTask.NAME)
                    .flatMap { it.outputFile }
            )
            ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializersProvider)
            dumpInfoConfiguration.set(extension.dumpInfo)
            usesRetrofitBuilderService(this.retrofitBuilderService)
        }

        project.tasks.register<UploadModuleSizesTask>(UploadModuleSizesTask.NAME) {
            val extension = project.extensions.getByType<TechBudgetExtension>()
            graphInfo.set(
                project.tasks.typedNamed<ModuleGraphTask>(GenerateModuleGraphTask.NAME)
                    .flatMap { it.outputFile }
            )
            ownerSerializer.set(project.requireCodeOwnershipExtension().ownerSerializersProvider)
            dumpInfoConfiguration.set(extension.dumpInfo)
            usesRetrofitBuilderService(this.retrofitBuilderService)
        }
    }
}
