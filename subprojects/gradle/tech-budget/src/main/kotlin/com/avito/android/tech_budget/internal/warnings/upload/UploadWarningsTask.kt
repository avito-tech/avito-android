package com.avito.android.tech_budget.internal.warnings.upload

import com.avito.android.OwnerSerializer
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.warnings.log.FileLogReader
import com.avito.android.tech_budget.internal.warnings.log.converter.LogToWarningConverter
import com.avito.android.tech_budget.internal.warnings.log.converter.ProjectInfoConverter
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

internal abstract class UploadWarningsTask : DefaultTask() {

    @get:Internal
    abstract val outputDirectory: DirectoryProperty

    @get:Internal
    abstract val warningsSeparator: Property<String>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializer>

    @get:Internal
    abstract val uploadWarningsBatchSize: Property<Int>

    @get:Internal
    abstract val uploadWarningsParallelRequestsCount: Property<Int>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.getLoggerFactory(this)

    @TaskAction
    fun uploadWarnings() {
        val logToWarningConverter = LogToWarningConverter()
        val logReader =
            FileLogReader(
                outputDirectory.get().asFile,
                warningsSeparator.get(),
                ProjectInfoConverter.default { ownerSerializer.get() }
            )

        val warnings = logReader.getAll()
            .map(logToWarningConverter::convert)

        val logger = loggerFactory.get().create("Warnings")
        if (warnings.isEmpty()) {
            logger.info("No warnings found")
        } else {
            logger.info("Found ${warnings.size} warnings. Uploading...")
            uploadCollectedWarnings(warnings)
        }
    }

    private fun uploadCollectedWarnings(warnings: List<Warning>) {
        val dumpConfiguration = dumpInfoConfiguration.get()

        val sender = UploadWarningsBatcher(
            batchSize = uploadWarningsBatchSize.get(),
            parallelRequestsCount = uploadWarningsParallelRequestsCount.get(),
            apiClient = UploadWarningsApi.create(
                baseUrl = dumpConfiguration.baseUploadUrl.get(),
                ownerSerializer = { ownerSerializer.get() },
                loggerFactory = loggerFactory.get()
            )
        )

        sender.send(DumpInfo.fromExtension(dumpConfiguration), warnings)
    }

    companion object {
        const val NAME = "uploadWarnings"
    }
}
