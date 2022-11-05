package com.avito.android.tech_budget.internal.warnings.upload

import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.warnings.log.FileLogReader
import com.avito.android.tech_budget.internal.warnings.log.converter.LogToWarningConverter
import com.avito.android.tech_budget.internal.warnings.log.converter.ProjectInfoConverter
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
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

    @TaskAction
    fun uploadWarnings() {
        val logToWarningConverter = LogToWarningConverter()
        val logReader =
            FileLogReader(outputDirectory.get().asFile, warningsSeparator.get(), ProjectInfoConverter.default())

        val warnings = logReader.getAll()
            .map(logToWarningConverter::convert)

        if (warnings.isEmpty()) {
            logger.lifecycle("No warnings found")
        } else {
            logger.lifecycle("Found ${warnings.size} warnings. Uploading...")
            uploadCollectedWarnings(warnings)
        }
    }

    private fun uploadCollectedWarnings(warnings: List<Warning>) {
        val dumpConfiguration = dumpInfoConfiguration.get()
        val sender = WarningsSender(dumpConfiguration.baseUploadUrl.get())
        sender.send(warnings, DumpInfo.fromExtension(dumpConfiguration))
    }

    companion object {
        const val NAME = "uploadWarnings"
    }
}
