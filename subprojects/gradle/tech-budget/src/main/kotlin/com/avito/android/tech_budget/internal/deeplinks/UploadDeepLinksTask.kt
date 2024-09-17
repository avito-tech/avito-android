package com.avito.android.tech_budget.internal.deeplinks

import com.avito.android.OwnerSerializerProvider
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.deeplinks.DeepLink
import com.avito.android.tech_budget.internal.deeplinks.models.UploadDeepLinksRequest
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.service.RetrofitBuilderService
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.android.tech_budget.parser.FileParser
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

internal abstract class UploadDeepLinksTask : DefaultTask() {

    @get:Internal
    abstract val deeplinksFileParser: Property<FileParser<DeepLink>>

    @get:InputFile
    abstract val deeplinksInput: RegularFileProperty

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @get:Internal
    abstract val retrofitBuilderService: Property<RetrofitBuilderService>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun uploadDeeplinks() {
        val deeplinks = deserializeDeeplinks()
        val logger = loggerFactory.get().create("DeepLinks")
        if (deeplinks.isEmpty()) {
            logger.info("No deepLinks found")
        } else {
            logger.info("Found ${deeplinks.size} deepLinks. Uploading...")
            upload(deeplinks)
        }
    }

    private fun deserializeDeeplinks(): List<DeepLink> {
        val deeplinksFile = deeplinksInput.get().asFile
        return deeplinksFileParser.get().parse(deeplinksFile)
    }

    private fun upload(deeplinks: List<DeepLink>) {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = retrofitBuilderService.get()
            .build(
                ownerAdapterFactory = OwnerAdapterFactory(ownerSerializer.get().provideIdSerializer()),
                loggerFactory = loggerFactory.get()
            ).create<UploadDeepLinksApi>()

        service.dumpDeepLinks(UploadDeepLinksRequest(DumpInfo.fromExtension(dumpInfoConfig), deeplinks))
            .executeWithHttpFailure("Upload deepLinks request failed")
    }

    companion object {
        const val NAME = "uploadDeepLinks"
    }
}
