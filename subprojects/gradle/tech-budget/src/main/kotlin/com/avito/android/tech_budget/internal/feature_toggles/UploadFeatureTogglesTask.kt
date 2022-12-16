package com.avito.android.tech_budget.internal.feature_toggles

import com.avito.android.OwnerSerializer
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.feature_toggles.FeatureToggle
import com.avito.android.tech_budget.internal.di.ApiServiceProvider
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.feature_toggles.models.UploadFeatureTogglesRequest
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

internal abstract class UploadFeatureTogglesTask : DefaultTask() {

    @get:Internal
    abstract val featureTogglesFileParser: Property<FileParser<FeatureToggle>>

    @get:InputFile
    abstract val featureTogglesInput: RegularFileProperty

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializer>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.getLoggerFactory(this)

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

    private fun upload(FeatureToggles: List<FeatureToggle>) {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = ApiServiceProvider(
            baseUrl = dumpInfoConfig.baseUploadUrl.get(),
            ownerSerializer = ownerSerializer.get(),
            loggerFactory = loggerFactory.get()
        ).provide<UploadFeatureTogglesApi>()

        service.dumpFeatureToggles(UploadFeatureTogglesRequest(DumpInfo.fromExtension(dumpInfoConfig), FeatureToggles))
            .executeWithHttpFailure("Upload Feature Toggles request failed")
    }

    companion object {
        const val NAME = "uploadFeatureToggles"
    }
}
