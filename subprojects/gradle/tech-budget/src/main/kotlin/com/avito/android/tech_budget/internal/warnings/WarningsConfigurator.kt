package com.avito.android.tech_budget.internal.warnings

import com.avito.android.tech_budget.CollectWarningsConfiguration
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.android.tech_budget.internal.owners.requireOwnersSerializer
import com.avito.android.tech_budget.internal.warnings.collect.CollectWarningsTask
import com.avito.android.tech_budget.internal.warnings.log.FileLogWriter
import com.avito.android.tech_budget.internal.warnings.log.LogFileProjectProvider
import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.log.TaskLogsDumper
import com.avito.android.tech_budget.internal.warnings.log.converter.ProjectInfoConverter
import com.avito.android.tech_budget.internal.warnings.task.DefaultTaskBuildOperationIdProvider
import com.avito.android.tech_budget.internal.warnings.upload.UploadWarningsTask
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.internal.logging.LoggingManagerInternal
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.gradle.util.Path
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class WarningsConfigurator : TechBudgetConfigurator {

    private val taskBuildOperationProvider = DefaultTaskBuildOperationIdProvider

    override fun configure(project: Project) {
        if (project.isRoot()) {
            configureUpload(project)
        } else {
            configureCollect(project)
        }
    }

    private fun configureUpload(root: Project) {
        val extension = root.extensions.getByType<TechBudgetExtension>()
        val collectWarnings = root.tasks.register<CollectWarningsTask>(CollectWarningsTask.NAME) {
            this.outputDirectory.set(extension.warnings.outputDirectory)
        }
        root.tasks.register<UploadWarningsTask>(UploadWarningsTask.NAME) {
            dependsOn(collectWarnings)
            this.outputDirectory.set(collectWarnings.get().outputDirectory)
            this.warningsSeparator.set(extension.warnings.warningsSeparator)
            this.dumpInfoConfiguration.set(extension.dumpInfo)
            this.ownerSerializer.set(root.requireCodeOwnershipExtension().ownerSerializer)
            this.uploadWarningsBatchSize.set(extension.warnings.uploadWarningsBatchSize)
            this.uploadWarningsParallelRequestsCount.set(extension.warnings.uploadWarningsParallelRequestsCount)
        }
        root.gradle.addBuildListener(taskBuildOperationProvider)
    }

    private fun configureCollect(subProject: Project) {
        require(subProject.rootProject.plugins.hasPlugin("com.avito.android.tech-budget")) {
            "Plugin `com.avito.android.tech-budget` must be applied to the root project"
        }
        val compileTasks = subProject.tasks.withType<KotlinCompile>()
        val extension = subProject.rootProject.extensions.getByType<TechBudgetExtension>()
        val warningsConfig = extension.warnings
        configureRootCollectWarnings(subProject.rootProject, compileTasks, warningsConfig.compileWarningsTaskNames)
        compileTasks.configureEach { compileTask ->
            collectWarnings(subProject, warningsConfig, compileTask)
        }
    }

    private fun configureRootCollectWarnings(
        root: Project,
        compileTasks: TaskCollection<KotlinCompile>,
        compileWarningsTaskNamesProp: Provider<Set<String>>
    ) {
        root.tasks.withType<CollectWarningsTask>().configureEach { collectWarningsTask ->
            val compileWarningsTaskNames = compileWarningsTaskNamesProp.get()
            val acceptedCompileTaskNames = compileTasks.names.filter { name -> compileWarningsTaskNames.contains(name) }
            acceptedCompileTaskNames.forEach { acceptedCompileTaskName ->
                collectWarningsTask.dependsOn(compileTasks.named(acceptedCompileTaskName))
            }
        }
    }

    private fun collectWarnings(
        subProject: Project,
        warningsConfig: CollectWarningsConfiguration,
        compileTask: KotlinCompile
    ) {
        val loggingManager = compileTask.logging as LoggingManagerInternal

        val codeOwnershipExtension = subProject.requireCodeOwnershipExtension()
        val projectInfo = ProjectInfo(
            path = subProject.path,
            owners = codeOwnershipExtension.owners.getOrElse(emptySet()),
        )

        val logDirectoryProvider = LogFileProjectProvider(
            rootOutputDir = warningsConfig.outputDirectory.get().asFile,
            projectInfo = projectInfo,
            taskName = compileTask.name,
            projectInfoConverter = ProjectInfoConverter.default { codeOwnershipExtension.requireOwnersSerializer() }
        )
        val logSaver = FileLogWriter(
            fileProvider = logDirectoryProvider, separator = warningsConfig.warningsSeparator.get()
        )
        loggingManager.addOutputEventListener(
            TaskLogsDumper(
                targetLogLevel = LogLevel.WARN,
                logWriter = logSaver,
                taskPath = Path.path(compileTask.path),
                taskBuildOperationIdProvider = taskBuildOperationProvider,
            )
        )
    }
}
