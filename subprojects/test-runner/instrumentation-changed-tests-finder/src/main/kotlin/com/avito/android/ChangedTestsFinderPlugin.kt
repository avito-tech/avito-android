package com.avito.android

import com.avito.android.InstrumentationChangedTestsFinderApi.changedTestsFinderExtensionName
import com.avito.android.InstrumentationChangedTestsFinderApi.changedTestsFinderTaskName
import com.avito.logger.GradleLoggerFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class ChangedTestsFinderPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<ChangedTestsFinderExtension>(changedTestsFinderExtensionName)

        target.tasks.register<FindChangedTestsTask>(changedTestsFinderTaskName) {
            group = "impact-analysis"

            targetCommit.set(extension.targetCommit)

            loggerFactory.set(
                GradleLoggerFactory.fromTask(
                    project = target,
                    taskName = this.name,
                    plugin = this@ChangedTestsFinderPlugin
                )
            )
        }
    }
}
