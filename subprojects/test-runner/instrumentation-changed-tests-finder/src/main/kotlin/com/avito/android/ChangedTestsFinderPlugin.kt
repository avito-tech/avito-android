package com.avito.android

import com.avito.android.InstrumentationChangedTestsFinderApi.changedTestsFinderExtensionName
import com.avito.android.InstrumentationChangedTestsFinderApi.changedTestsFinderTaskName
import com.avito.git.gitInfoService
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

public class ChangedTestsFinderPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<ChangedTestsFinderExtension>(changedTestsFinderExtensionName)

        target.gitInfoService()

        target.tasks.register<FindChangedTestsTask>(changedTestsFinderTaskName) {
            group = "impact-analysis"
            targetCommit.set(extension.targetCommit)
        }
    }
}
