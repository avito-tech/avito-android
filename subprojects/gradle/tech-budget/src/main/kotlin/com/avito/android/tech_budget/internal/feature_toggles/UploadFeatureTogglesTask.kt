package com.avito.android.tech_budget.internal.feature_toggles

import com.avito.android.OwnerSerializerProvider
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.feature_toggles.FeatureToggle
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.feature_toggles.models.UploadFeatureTogglesRequest
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

internal abstract class UploadFeatureTogglesTask : DefaultTask() {

    @get:Internal
    abstract val featureTogglesFileParser: Property<FileParser<FeatureToggle>>

    @get:InputFile
    abstract val featureTogglesInput: RegularFileProperty

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @get:Internal
    abstract val retrofitBuilderService: Property<RetrofitBuilderService>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun upload() {
        val featureToggles = deserializeFeatureToggles()
        val logger = loggerFactory.get().create("FeatureToggles")
        if (featureToggles.isEmpty()) {
            logger.info("No Feature Toggles found")
        } else {
            logger.info("Found ${featureToggles.size} Feature Toggles. Uploading...")
            upload(featureToggles)
        }
    }

    private fun deserializeFeatureToggles(): List<FeatureToggle> {
        val featureTogglesFile = featureTogglesInput.get().asFile
        return featureTogglesFileParser.get().parse(featureTogglesFile)
    }

    private fun upload(featureToggles: List<FeatureToggle>) {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = retrofitBuilderService.get()
            .build(
                ownerAdapterFactory = OwnerAdapterFactory(ownerSerializer.get().provideIdSerializer()),
                loggerFactory = loggerFactory.get()
            )
            .create<UploadFeatureTogglesApi>()

        service.dumpFeatureToggles(UploadFeatureTogglesRequest(DumpInfo.fromExtension(dumpInfoConfig), featureToggles))
            .executeWithHttpFailure("Upload Feature Toggles request failed")
    }

    companion object {
        const val NAME = "uploadFeatureToggles"
    }
}
