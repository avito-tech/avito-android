package com.avito.instrumentation.internal

import com.avito.instrumentation.InstrumentationTestsTask
import com.avito.instrumentation.configuration.InstrumentationConfiguration
import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

internal class OutputDirConfigurator(
    private val extension: InstrumentationTestsPluginExtension,
    private val reportResolver: ReportResolver,
    private val outputDirResolver: OutputDirResolver,
    private val configuration: InstrumentationConfiguration,
) : InstrumentationTaskConfigurator {

    override fun configure(task: InstrumentationTestsTask) {
        task.output.set(resolve(configuration))
        task.macrobenchmarkOutputDirectory.set(extension.macrobenchmark.outputDir)
    }

    fun resolve(configuration: InstrumentationConfiguration): Provider<Directory> {
        return outputDirResolver.resolveWithDeprecatedProperty().map {
            it.dir("${reportResolver.getRunId()}/${configuration.name}")
        }
    }
}
