package com.avito.android.tech_budget.internal.warnings.upload

import com.avito.android.OwnerSerializer
import com.avito.android.owner.adapter.DefaultOwnerAdapter
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.di.ApiServiceProvider
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.warnings.report.ProjectInfo
import com.avito.android.tech_budget.internal.warnings.report.converter.IssueToWarningConverter
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import com.avito.android.tech_budget.parser.FileParser
import com.avito.android.tech_budget.warnings.CompilerIssue
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

internal abstract class UploadWarningsTask : DefaultTask() {

    @get:Input
    abstract val inputReports: MapProperty<ProjectInfo, RegularFileProperty>

    @get:Internal
    abstract val issuesFileParser: Property<FileParser<CompilerIssue>>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializer>

    @get:Internal
    abstract val uploadWarningsBatchSize: Property<Int>

    @get:Internal
    abstract val uploadWarningsParallelRequestsCount: Property<Int>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun uploadWarnings() {
        val logToWarningConverter = IssueToWarningConverter(issuesFileParser.get())
        val warnings = logToWarningConverter.convert(inputReports.get())

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
            apiClient = ApiServiceProvider(
                baseUrl = dumpConfiguration.baseUploadUrl.get(),
                ownerAdapter = DefaultOwnerAdapter { ownerSerializer.get() },
                loggerFactory = loggerFactory.get()
            ).provide()
        )

        sender.send(DumpInfo.fromExtension(dumpConfiguration), warnings)
    }

    companion object {
        const val NAME = "uploadWarnings"
    }
}
