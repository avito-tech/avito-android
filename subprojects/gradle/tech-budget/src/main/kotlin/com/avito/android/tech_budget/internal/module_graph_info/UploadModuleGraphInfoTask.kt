package com.avito.android.tech_budget.internal.module_graph_info

import com.avito.android.OwnerSerializerProvider
import com.avito.android.module_graph.models.ModuleGraphInfo
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.module_graph_info.models.Dependency
import com.avito.android.tech_budget.internal.module_graph_info.models.ModuleDemoAppDependency
import com.avito.android.tech_budget.internal.module_graph_info.models.UploadModuleGraphInfoRequest
import com.avito.android.tech_budget.internal.service.RetrofitBuilderService
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import retrofit2.create

internal abstract class UploadModuleGraphInfoTask : DefaultTask() {

    @get:InputFile
    abstract val graphInfo: RegularFileProperty

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:Internal
    abstract val retrofitBuilderService: Property<RetrofitBuilderService>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    private val defaultJson: Json by lazy {
        Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        }
    }

    @TaskAction
    fun upload() {
        val service = retrofitBuilderService.get()
            .build(
                ownerAdapterFactory = OwnerAdapterFactory(ownerSerializer.get().provideIdSerializer()),
                loggerFactory = loggerFactory.get()
            )
            .create<UploadModuleGraphInfoApi>()

        val moduleGraphInfo = parseModuleGraphInfoFromFile()

        service
            .dumpModuleGraphInfo(moduleGraphInfo.toRequest())
            .executeWithHttpFailure("Upload Module Graph Info request failed")
    }

    private fun parseModuleGraphInfoFromFile(): ModuleGraphInfo {
        val graphInfoFile = graphInfo.get().asFile
        require(graphInfoFile.exists()) {
            "module-graph.json file doesn't exist"
        }
        return defaultJson.decodeFromString<ModuleGraphInfo>(graphInfoFile.readText())
    }

    private fun ModuleGraphInfo.toRequest(): UploadModuleGraphInfoRequest {
        return UploadModuleGraphInfoRequest(
            dumpInfo = DumpInfo.fromExtension(dumpInfoConfiguration.get()),
            dependencies = dependencies.map { Dependency(from = it.from, to = it.to, type = it.type) },
            sizes = sizes,
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
        const val NAME = "uploadModuleGraphInfo"
    }
}
