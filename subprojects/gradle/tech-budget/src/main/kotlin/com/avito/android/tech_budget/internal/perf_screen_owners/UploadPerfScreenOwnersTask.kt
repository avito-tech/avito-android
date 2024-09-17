package com.avito.android.tech_budget.internal.perf_screen_owners

import com.avito.android.OwnerSerializerProvider
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.perf_screen_owners.models.UploadPerfScreenOwnersRequest
import com.avito.android.tech_budget.internal.service.RetrofitBuilderService
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.android.tech_budget.parser.FileParser
import com.avito.android.tech_budget.perf_screen_owners.PerformanceScreenInfo
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import retrofit2.create

internal abstract class UploadPerfScreenOwnersTask : DefaultTask() {

    @get:Internal
    abstract val perfOwnersFileParser: Property<FileParser<PerformanceScreenInfo>>

    @get:InputFile
    abstract val perfOwnersInput: RegularFileProperty

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @get:Internal
    abstract val retrofitBuilderService: Property<RetrofitBuilderService>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun uploadPerfOwners() {
        val perfOwners = deserializePerfOwners()
        val logger = loggerFactory.get().create("Perf Screen Owners")
        if (perfOwners.isEmpty()) {
            logger.info("No perf owners found")
        } else {
            logger.info("Found ${perfOwners.size} perf screens. Uploading...")
            upload(perfOwners)
        }
    }

    private fun deserializePerfOwners(): List<PerformanceScreenInfo> {
        val perfOwnersFile = perfOwnersInput.get().asFile
        return perfOwnersFileParser.get().parse(perfOwnersFile)
    }

    private fun upload(screenOwners: List<PerformanceScreenInfo>) {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = retrofitBuilderService.get()
            .build(
                ownerAdapterFactory = OwnerAdapterFactory(ownerSerializer.get().provideIdSerializer()),
                loggerFactory = loggerFactory.get()
            )
            .create<UploadPerfScreenOwnersApi>()

        service.dumpPerfOwners(UploadPerfScreenOwnersRequest(DumpInfo.fromExtension(dumpInfoConfig), screenOwners))
            .executeWithHttpFailure("Upload performance screen owners request failed")
    }

    companion object {
        const val NAME = "uploadPerfScreensOwners"
    }
}
