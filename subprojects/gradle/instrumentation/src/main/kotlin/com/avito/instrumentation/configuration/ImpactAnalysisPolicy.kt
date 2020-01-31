package com.avito.instrumentation.configuration

import com.avito.instrumentation.impact.AnalyzeTestImpact
import com.avito.instrumentation.impact.analyzeTestImpactTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.Serializable

@Suppress("UnstableApiUsage")
sealed class ImpactAnalysisPolicy : Serializable {

    sealed class On : ImpactAnalysisPolicy() {

        abstract fun getArtifact(task: AnalyzeTestImpact): Provider<RegularFile>

        fun getTask(project: Project): TaskProvider<AnalyzeTestImpact> = project.tasks.analyzeTestImpactTask()

        object RunAffectedTests : On() {
            override fun getArtifact(task: AnalyzeTestImpact): Provider<RegularFile> = task.testsToRunFile
        }

        object RunNewTests : On() {
            override fun getArtifact(task: AnalyzeTestImpact): Provider<RegularFile> = task.addedTestsFile
        }
    }

    object Off : ImpactAnalysisPolicy()
}
