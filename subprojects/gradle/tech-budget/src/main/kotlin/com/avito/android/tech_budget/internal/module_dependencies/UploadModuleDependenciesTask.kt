package com.avito.android.tech_budget.internal.module_dependencies

import com.avito.android.OwnerSerializer
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.di.ApiServiceProvider
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.module_dependencies.models.UploadModuleDependenciesRequest
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

internal abstract class UploadModuleDependenciesTask : DefaultTask() {

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializer>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.getLoggerFactory(this)

    @TaskAction
    fun uploadModuleDependencies() {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = ApiServiceProvider(
            baseUrl = dumpInfoConfig.baseUploadUrl.get(),
            ownerSerializer = ownerSerializer.get(),
            loggerFactory = loggerFactory.get()
        ).provide<UploadModuleDependenciesApi>()

        service.dumpModuleDependencies(
            UploadModuleDependenciesRequest(
                dumpInfo = DumpInfo.fromExtension(dumpInfoConfig),
                dependencies = ModuleDependenciesCollector.collect(project)
            )
        ).executeWithHttpFailure("Upload Module Dependencies request failed")
    }

    companion object {
        const val NAME = "uploadModuleDependencies"
    }
}
