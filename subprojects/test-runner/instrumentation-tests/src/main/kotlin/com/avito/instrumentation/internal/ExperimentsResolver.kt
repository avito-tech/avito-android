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
            saveTestArtifactsToOutputs = getSaveTestArtifactsToOutputs(extension),
            fetchLogcatForIncompleteTests = getFetchLogcatForIncompleteTests(extension),
            uploadArtifactsFromRunner = getUploadArtifactsFromRunner(extension),
            useLegacyExtensionsV1Beta = getUseLegacyExtensionsV1Beta(extension),
            sendPodsMetrics = getSendPodsMetrics(extension)
        )
    }

    private fun getSendPodsMetrics(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.sendPodsMetrics.getOrElse(false)
    }

    private fun getSaveTestArtifactsToOutputs(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.saveTestArtifactsToOutputs.getOrElse(false)
    }

    private fun getFetchLogcatForIncompleteTests(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.fetchLogcatForIncompleteTests.getOrElse(false)
    }

    private fun getUseLegacyExtensionsV1Beta(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.useLegacyExtensionsV1Beta.getOrElse(true)
    }

    private fun getUploadArtifactsFromRunner(extension: GradleInstrumentationPluginConfiguration): Boolean {
        return extension.experimental.uploadArtifactsFromRunner.getOrElse(
            project.getBooleanProperty("avito.report.fromRunner", default = false)
        )
    }
}
