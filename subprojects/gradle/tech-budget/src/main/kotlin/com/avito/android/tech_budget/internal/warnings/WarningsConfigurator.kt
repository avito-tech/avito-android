package com.avito.android.tech_budget.internal.warnings

import com.avito.android.CodeOwnershipExtension
import com.avito.android.tech_budget.CollectWarningsConfiguration
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.warnings.collect.CollectWarningsTask
import com.avito.android.tech_budget.internal.warnings.log.FileLogWriter
import com.avito.android.tech_budget.internal.warnings.log.LogFileProjectProvider
import com.avito.android.tech_budget.internal.warnings.log.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.log.TaskLogsDumper
import com.avito.android.tech_budget.internal.warnings.log.converter.ProjectInfoConverter
import com.avito.android.tech_budget.internal.warnings.upload.UploadWarningsTask
import com.avito.kotlin.dsl.withType
import org.gradle.api.Project
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskCollection
import org.gradle.internal.logging.LoggingManagerInternal
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

internal class WarningsConfigurator : TechBudgetConfigurator() {

    override fun doConfigureUpload(root: Project) {
        val extension = root.extensions.create<TechBudgetExtension>("techBudget")
        val collectWarnings = root.tasks.register<CollectWarningsTask>(CollectWarningsTask.NAME) {
            this.outputDirectory.set(extension.warnings.outputDirectory)
        }
        root.tasks.register<UploadWarningsTask>(UploadWarningsTask.NAME) {
            dependsOn(collectWarnings)
            this.outputDirectory.set(collectWarnings.get().outputDirectory)
            this.warningsSeparator.set(extension.warnings.warningsSeparator)
            this.dumpInfoConfiguration.set(extension.dumpInfo)
        }
    }

    override fun doConfigureCollect(subProject: Project) {
        require(subProject.rootProject.plugins.hasPlugin("com.avito.android.tech-budget")) {
            "Plugin `com.avito.android.tech-budget` must be applied to the root project"
        }
        val compileTasks = subProject.tasks.withType<KotlinCompile>()
        val warningsConfig = subProject.rootProject.extensions.getByType<TechBudgetExtension>().warnings
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
        val codeOwnershipExtension = requireNotNull(subProject.extensions.findByType<CodeOwnershipExtension>()) {
            "You must apply plugin `com.avito.android.code-ownership` to the root project to run this task"
        }
        val projectInfo = ProjectInfo(
            path = subProject.path,
            // TODO Serialize / Deserialize owners in separate entity MA-2868
            owners = codeOwnershipExtension.owners.get().map { it.toString() },
        )

        val logDirectoryProvider = LogFileProjectProvider(
            rootOutputDir = warningsConfig.outputDirectory.get().asFile,
            projectInfo = projectInfo,
            taskName = compileTask.name,
            projectInfoConverter = ProjectInfoConverter.default()
        )
        val logSaver = FileLogWriter(
            fileProvider = logDirectoryProvider,
            separator = warningsConfig.warningsSeparator.get()
        )
        loggingManager.addOutputEventListener(
            TaskLogsDumper(
                targetLogLevel = LogLevel.WARN,
                logWriter = logSaver
            )
        )
    }
}
