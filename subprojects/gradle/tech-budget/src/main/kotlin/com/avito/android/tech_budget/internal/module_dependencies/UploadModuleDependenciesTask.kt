package com.avito.android.tech_budget.internal.module_dependencies

import com.avito.android.OwnerSerializerProvider
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.module_dependencies.models.UploadModuleDependenciesRequest
import com.avito.android.tech_budget.internal.service.RetrofitBuilderService
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import retrofit2.create

internal abstract class UploadModuleDependenciesTask : DefaultTask() {

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @get:Internal
    abstract val retrofitBuilderService: Property<RetrofitBuilderService>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun uploadModuleDependencies() {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = retrofitBuilderService.get()
            .build(
                ownerAdapterFactory = OwnerAdapterFactory(ownerSerializer.get().provideIdSerializer()),
                loggerFactory = loggerFactory.get()
            )
            .create<UploadModuleDependenciesApi>()

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
