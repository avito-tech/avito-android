package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.Experiments
import com.avito.instrumentation.configuration.InstrumentationPluginConfiguration.GradleInstrumentationPluginConfiguration
import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.Project

internal class ExperimentsResolver(private val project: Project) {

    fun getExperiments(
        extension: GradleInstrumentationPluginConfiguration
    ): Experiments {
        return Experiments(
            useInMemoryReport = getUseInMemoryReport(extension),
            saveTestArtifactsToOutputs = getSaveTestArtifactsToOutputs(extension),
            fetchLogcatForIncompleteTests = getFetchLogcatForIncompleteTests(extension),
            uploadArtifactsFromRunner = getUploadArtifactsFromRunner(extension),
        )
    }

    private fun getUseInMemoryReport(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.useInMemoryReport.getOrElse(false)
    }

    private fun getSaveTestArtifactsToOutputs(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.saveTestArtifactsToOutputs.getOrElse(false)
    }

    private fun getFetchLogcatForIncompleteTests(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.fetchLogcatForIncompleteTests.getOrElse(false)
    }

    private fun getUploadArtifactsFromRunner(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.uploadArtifactsFromRunner.getOrElse(
            project.getBooleanProperty("avito.report.fromRunner", default = false)
        )
    }
}
