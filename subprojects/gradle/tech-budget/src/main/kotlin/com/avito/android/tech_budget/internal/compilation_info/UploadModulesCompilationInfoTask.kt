package com.avito.android.tech_budget.internal.compilation_info

import com.avito.android.OwnerSerializer
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.compilation_info.models.ModuleCompilationInfo
import com.avito.android.tech_budget.internal.compilation_info.models.UploadModulesCompilationInfoRequest
import com.avito.android.tech_budget.internal.di.ApiServiceProvider
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import com.avito.tech_budget.compilation_info.ModuleCompileTime
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import java.lang.reflect.Type

internal abstract class UploadModulesCompilationInfoTask : DefaultTask() {

    @get:Internal
    abstract val ownerSerializer: Property<OwnerSerializer>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @get:InputFile
    abstract val compilationTimeFile: RegularFileProperty

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun uploadModulesCompileTime() {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = ApiServiceProvider(
            baseUrl = dumpInfoConfig.baseUploadUrl.get(),
            ownerSerializer = ownerSerializer.get(),
            loggerFactory = loggerFactory.get()
        ).provide<UploadModulesCompilationInfoApi>()

        service.dumpModulesInfo(
            UploadModulesCompilationInfoRequest(
                dumpInfo = DumpInfo.fromExtension(dumpInfoConfig),
                compilationInfo = getCompilationInfo()
            )
        ).executeWithHttpFailure("Upload Module Compilation Info request failed")
    }

    private fun getCompilationInfo(): List<ModuleCompilationInfo> {
        val compilationTimeFile = compilationTimeFile.get().asFile
        val compilationTimeJson = compilationTimeFile.readText()
        val type: Type = Types.newParameterizedType(List::class.java, ModuleCompileTime::class.java)
        val modulesCompileTime = Moshi.Builder()
            .build()
            .adapter<List<ModuleCompileTime>>(type)
            .fromJson(compilationTimeJson)
        require(!modulesCompileTime.isNullOrEmpty()) {
            "Unable to read compilation time from file ${compilationTimeFile.path}"
        }
        require(isUniqueModules(modulesCompileTime.map { it.modulePath })) {
            "Some modules presented more than one time"
        }
        return modulesCompileTime.map {
            ModuleCompilationInfo(it.modulePath, it.compileTimeMs)
        }
    }

    private fun isUniqueModules(modules: List<String>): Boolean {
        return modules.toSet().size == modules.size
    }

    companion object {
        const val NAME = "uploadModuleCompilationInfo"
    }
}
