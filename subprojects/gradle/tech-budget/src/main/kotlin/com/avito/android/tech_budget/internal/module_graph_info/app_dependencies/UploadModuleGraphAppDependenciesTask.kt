package com.avito.android.tech_budget.internal.module_graph_info.app_dependencies

import com.avito.android.OwnerSerializerProvider
import com.avito.android.module_graph.models.ModuleGraphInfo
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.module_graph_info.UploadModuleGraphInfoApi
import com.avito.android.tech_budget.internal.module_graph_info.UploadModuleGraphInfoParser
import com.avito.android.tech_budget.internal.service.RetrofitBuilderService
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
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

internal abstract class UploadModuleGraphAppDependenciesTask : DefaultTask() {

    @get:InputFile
    abstract val graphInfo: RegularFileProperty

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:Internal
    abstract val retrofitBuilderService: Property<RetrofitBuilderService>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    private val graphInfoParser by lazy { UploadModuleGraphInfoParser() }

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun upload() {
        val service = retrofitBuilderService.get()
            .build(
                ownerAdapterFactory = OwnerAdapterFactory(ownerSerializer.get().provideIdSerializer()),
                loggerFactory = loggerFactory.get()
            )
            .create<UploadModuleGraphInfoApi>()

        val moduleGraphInfo = graphInfoParser.parseModuleGraphInfoFromFile(graphInfo.get().asFile)

        service
            .dumpModuleGraphAppDependencies(moduleGraphInfo.toRequest())
            .executeWithHttpFailure("UploadModuleGraphAppDependencies request failed")
    }

    private fun ModuleGraphInfo.toRequest(): UploadModuleGraphAppDependenciesRequest {
        return UploadModuleGraphAppDependenciesRequest(
            dumpInfo = DumpInfo.fromExtension(dumpInfoConfiguration.get()),
            modulesToDemoApps = modulesToDemoApps.flatMap { (module, demoApps) ->
                demoApps.map {
                    ModuleDemoAppDependency(
                        module = module,
                        demoApp = it,
                    )
                }
            }.toList()
        )
    }

    companion object {
        const val NAME = "uploadModuleGraphAppDependencies"
    }
}
