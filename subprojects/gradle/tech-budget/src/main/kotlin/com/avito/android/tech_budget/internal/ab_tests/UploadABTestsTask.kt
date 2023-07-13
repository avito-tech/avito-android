package com.avito.android.tech_budget.internal.ab_tests

import com.avito.android.OwnerSerializerProvider
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.ab_tests.ABTest
import com.avito.android.tech_budget.internal.ab_tests.models.UploadABTestsRequest
import com.avito.android.tech_budget.internal.di.ApiServiceProvider
import com.avito.android.tech_budget.internal.dump.DumpInfo
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

internal abstract class UploadABTestsTask : DefaultTask() {

    @get:Internal
    abstract val abTestsFileParser: Property<FileParser<ABTest>>

    @get:InputFile
    abstract val abTestsInput: RegularFileProperty

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun upload() {
        val abTests = deserializeABTests()
        val logger = loggerFactory.get().create("ABTests")
        if (abTests.isEmpty()) {
            logger.info("No AB tests found")
        } else {
            logger.info("Found ${abTests.size} AB tests. Uploading...")
            upload(abTests)
        }
    }

    private fun deserializeABTests(): List<ABTest> {
        val abTestsFile = abTestsInput.get().asFile
        return abTestsFileParser.get().parse(abTestsFile)
    }

    private fun upload(abTests: List<ABTest>) {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = ApiServiceProvider(
            baseUrl = dumpInfoConfig.baseUploadUrl.get(),
            ownerSerializer = ownerSerializer.get(),
            loggerFactory = loggerFactory.get()
        ).provide<UploadABTestsApi>()

        service.dumpABTests(UploadABTestsRequest(DumpInfo.fromExtension(dumpInfoConfig), abTests))
            .executeWithHttpFailure("Upload AB Tests request failed")
    }

    companion object {
        const val NAME = "uploadABTests"
    }
}
