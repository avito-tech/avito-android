package com.avito.android.tech_budget.internal.warnings.upload

import com.avito.android.OwnerSerializer
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.android.tech_budget.internal.warnings.log.FileLogReader
import com.avito.android.tech_budget.internal.warnings.log.converter.LogToWarningConverter
import com.avito.android.tech_budget.internal.warnings.log.converter.ProjectInfoConverter
import com.avito.android.tech_budget.internal.warnings.upload.model.Warning
import com.avito.android.tech_budget.internal.warnings.upload.model.WarningsRequestBody
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

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializer>

    @TaskAction
    fun uploadWarnings() {
        val logToWarningConverter = LogToWarningConverter()
        val ownersSerializer = ownerSerializer.get()
        val logReader =
            FileLogReader(
                outputDirectory.get().asFile,
                warningsSeparator.get(),
                ProjectInfoConverter.default { ownersSerializer }
            )

        val warnings = logReader.getAll()
            .map(logToWarningConverter::convert)

        if (warnings.isEmpty()) {
            logger.lifecycle("No warnings found")
        } else {
            logger.lifecycle("Found ${warnings.size} warnings. Uploading...")
            uploadCollectedWarnings(warnings, ownersSerializer)
        }
    }

    private fun uploadCollectedWarnings(warnings: List<Warning>, ownerSerializer: OwnerSerializer) {
        val dumpConfiguration = dumpInfoConfiguration.get()
        val sender = UploadWarningsApi.create(dumpConfiguration.baseUploadUrl.get(), ownerSerializer)
        sender.dumpWarnings(WarningsRequestBody(DumpInfo.fromExtension(dumpConfiguration), warnings))
            .executeWithHttpFailure(errorMessage = "Upload warnings request failed")
    }

    companion object {
        const val NAME = "uploadWarnings"
    }
}
