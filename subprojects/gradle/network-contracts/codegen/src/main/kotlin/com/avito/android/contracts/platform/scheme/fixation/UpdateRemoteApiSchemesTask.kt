package com.avito.android.contracts.platform.scheme.fixation

import com.avito.android.contracts.platform.analytics.trackFixationDuration
import com.avito.android.contracts.platform.internal.analytics.NetworkContractsAnalyticsService
import com.avito.android.contracts.platform.scheme.collect.ApiSchemesMetadata
import com.avito.logger.Logger
import com.avito.logger.LoggerFactory
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.time.measureTime

public abstract class UpdateRemoteApiSchemesTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val schemes: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val validationReports: ConfigurableFileCollection

    @get:OutputFile
    public abstract val outputFile: RegularFileProperty

    @get:Internal
    internal abstract val upsertService: Property<UpsertService<*>>

    @get:Internal
    internal abstract val analyticsTrackerService: Property<NetworkContractsAnalyticsService>

    @get:Internal
    internal abstract val loggerFactory: Property<LoggerFactory>

    private val logger: Logger by lazy { loggerFactory.get().create(UpdateRemoteApiSchemesTask::class.java.simpleName) }

    @TaskAction
    public fun upsert() {
        val tracker = analyticsTrackerService.get().tracker

        val elapsedTime = measureTime { innerUpsert() }
        tracker.trackFixationDuration(elapsedTime)

        outputFile.get().asFile.writeText("OK")
    }

    private fun innerUpsert() {
        val validationFailed = validationReports
            .filter { it.exists() }
            .any { it.readText() != "OK" }

        if (validationFailed) {
            error("Validation schemes failed.")
        }

        val schemes = schemes.filter(File::exists)
        if (schemes.isEmpty) {
            logger.warn("Schemes not found")
            return
        }

        val schemesMetadata = schemes
            .map { schema -> schema.inputStream().use { Json.decodeFromStream<ApiSchemesMetadata>(it) } }

        runBlocking {
            upsertService.get()?.sendContracts(
                schemes = schemesMetadata,
            )
        }
    }
}
