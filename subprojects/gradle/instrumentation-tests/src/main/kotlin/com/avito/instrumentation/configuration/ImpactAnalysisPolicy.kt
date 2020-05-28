package com.avito.instrumentation.configuration

import com.avito.instrumentation.impact.AnalyzeTestImpactTask
import com.avito.instrumentation.impact.analyzeTestImpactTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.Serializable

@Suppress("UnstableApiUsage")
sealed class ImpactAnalysisPolicy : Serializable {

    sealed class On : ImpactAnalysisPolicy() {

        abstract fun getArtifact(task: AnalyzeTestImpactTask): Provider<RegularFile>

        fun getTask(project: Project): TaskProvider<AnalyzeTestImpactTask> = project.tasks.analyzeTestImpactTask()

        object RunAffectedTests : On() {
            override fun getArtifact(task: AnalyzeTestImpactTask): Provider<RegularFile> = task.testsToRunFile
        }

        object RunNewTests : On() {
            override fun getArtifact(task: AnalyzeTestImpactTask): Provider<RegularFile> = task.addedTestsFile
        }

        object RunModifiedTests : On() {
            override fun getArtifact(task: AnalyzeTestImpactTask): Provider<RegularFile> = task.modifiedTestsFile
        }
    }

    object Off : ImpactAnalysisPolicy()
}
