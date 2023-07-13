package com.avito.android.tech_budget.internal.lint_issues

import com.android.build.gradle.internal.lint.AndroidLintGlobalTask
import com.android.build.gradle.internal.lint.AndroidLintTask
import com.avito.android.isAndroidApp
import com.avito.android.tech_budget.TechBudgetExtension
import com.avito.android.tech_budget.internal.TechBudgetConfigurator
import com.avito.android.tech_budget.internal.lint_issues.collect.CollectLintIssuesTask
import com.avito.android.tech_budget.internal.lint_issues.upload.UploadLintIssuesTask
import com.avito.android.tech_budget.internal.owners.requireCodeOwnershipExtension
import com.avito.kotlin.dsl.isRoot
import com.avito.kotlin.dsl.typedNamedOrNull
import com.avito.kotlin.dsl.withType
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

internal class LintIssuesConfigurator : TechBudgetConfigurator {
    override fun configure(project: Project) {
        if (project.isRoot()) {
            configureUpload(project)
        } else if (project.isAndroidApp()) {
            configureCollect(project)
        }
    }

    private fun configureUpload(root: Project) {
        val extension = root.extensions.getByType<TechBudgetExtension>()
        val collectLintIssues = root.tasks.register<CollectLintIssuesTask>(CollectLintIssuesTask.NAME)

        root.tasks.register<UploadLintIssuesTask>(UploadLintIssuesTask.NAME) {
            dependsOn(collectLintIssues)
            this.ownerSerializer.set(root.requireCodeOwnershipExtension().ownerSerializersProvider)
            this.dumpInfoConfiguration.set(extension.dumpInfo)
            this.outputXmlFiles.set(collectLintIssues.get().outputXmlFiles)
        }
    }

    private fun configureCollect(subProject: Project) {
        require(subProject.rootProject.plugins.hasPlugin("com.avito.android.tech-budget")) {
            "Plugin `com.avito.android.tech-budget` must be applied to the root project"
        }

        val lintTaskProvider = subProject.tasks.typedNamedOrNull<AndroidLintGlobalTask>("lint")
        requireNotNull(lintTaskProvider) {
            "The project doesn't contain 'lint' task"
        }

        subProject.rootProject.tasks.withType<CollectLintIssuesTask>().configureEach { collectIssuesTask ->
            collectIssuesTask.dependsOn(lintTaskProvider)

            val reportTasks = subProject.tasks
                .withType<AndroidLintTask>()
                .matching { it.name.contains("lintReport") }

            require(reportTasks.isNotEmpty()) {
                "The project doesn't contain any of 'lintReport' task"
            }

            reportTasks.configureEach {
                require(it.xmlReportEnabled.get()) {
                    "The 'collectLintIssues' task requires lintConfig.xmlReport to be enabled for ${it.name}"
                }

                it.doLast(object : Action<Task> {
                    override fun execute(t: Task) {
                        collectIssuesTask.outputXmlFiles.from(it.xmlReportOutputFile.get().asFile)
                    }
                })
            }
        }
    }
}
