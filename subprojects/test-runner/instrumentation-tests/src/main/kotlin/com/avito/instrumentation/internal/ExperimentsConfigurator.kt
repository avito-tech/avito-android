package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.Experiments
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import com.avito.kotlin.dsl.getBooleanProperty
import org.gradle.api.Project

internal class ExperimentsConfigurator(
    private val project: Project,
    private val extension: InstrumentationTestsPluginExtension
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        task.experiments.set(getExperiments())
    }

    private fun getExperiments(): Experiments {
        return Experiments(
            saveTestArtifactsToOutputs = getSaveTestArtifactsToOutputs(extension),
            fetchLogcatForIncompleteTests = getFetchLogcatForIncompleteTests(extension),
            uploadArtifactsFromRunner = getUploadArtifactsFromRunner(extension),
            useLegacyExtensionsV1Beta = getUseLegacyExtensionsV1Beta(extension),
            sendPodsMetrics = getSendPodsMetrics(extension)
        )
    }

    private fun getSendPodsMetrics(extension: InstrumentationTestsPluginExtension): Boolean {
        return extension.experimental.sendPodsMetrics.getOrElse(true)
    }

    private fun getSaveTestArtifactsToOutputs(extension: InstrumentationTestsPluginExtension): Boolean {
        return extension.experimental.saveTestArtifactsToOutputs.getOrElse(false)
    }

    private fun getFetchLogcatForIncompleteTests(extension: InstrumentationTestsPluginExtension): Boolean {
        return extension.experimental.fetchLogcatForIncompleteTests.getOrElse(false)
    }

    private fun getUseLegacyExtensionsV1Beta(extension: InstrumentationTestsPluginExtension): Boolean {
        return extension.experimental.useLegacyExtensionsV1Beta.getOrElse(true)
    }

    private fun getUploadArtifactsFromRunner(extension: InstrumentationTestsPluginExtension): Boolean {
        return extension.experimental.uploadArtifactsFromRunner.getOrElse(
            project.getBooleanProperty("avito.report.fromRunner", default = false)
        )
    }
}
