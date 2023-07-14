package com.avito.android.tech_budget.internal.owners.dependencies

import com.avito.android.OwnerSerializerProvider
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.owner.dependency.JsonOwnedDependenciesSerializer
import com.avito.android.owner.dependency.OwnedDependency
import com.avito.android.serializers.OwnerIdSerializer
import com.avito.android.serializers.OwnerNameSerializer
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.di.ApiServiceProvider
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.owners.dependencies.models.UploadDependenciesRequestBody
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import com.avito.logger.GradleLoggerPlugin
import com.avito.logger.LoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class UploadDependenciesTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val externalDependencies: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val internalDependencies: RegularFileProperty

    @get:Input
    abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    private val loggerFactory: Provider<LoggerFactory> = GradleLoggerPlugin.provideLoggerFactory(this)

    @TaskAction
    fun uploadDependencies() {
        val ownerSerializerProvider = ownerSerializer.get()
        val ownerNameSerializer = ownerSerializerProvider.provideNameSerializer()
        val ownerIdSerializer = ownerSerializerProvider.provideIdSerializer()
        val internalDeps = extractDependencies(internalDependencies.asFile.get(), ownerNameSerializer)
        val externalDeps = extractDependencies(externalDependencies.asFile.get(), ownerNameSerializer)
        val loggerFactory = loggerFactory.get()
        val logger = loggerFactory.create("Dependencies")
        logger.info("Found ${internalDeps.size} internal and ${externalDeps.size} external dependencies")
        uploadDependencies(internalDeps + externalDeps, loggerFactory, ownerIdSerializer)
    }

    private fun extractDependencies(file: File, ownerSerializer: OwnerNameSerializer): List<OwnedDependency> {
        val ownedDependenciesSerializer = JsonOwnedDependenciesSerializer(OwnerAdapterFactory(ownerSerializer))
        val rawOwners = file.readText()
        return ownedDependenciesSerializer.deserialize(rawOwners)
    }

    private fun uploadDependencies(
        dependencies: List<OwnedDependency>,
        loggerFactory: LoggerFactory,
        ownerSerializer: OwnerIdSerializer,
    ) {
        val dumpInfoConfig = dumpInfoConfiguration.get()
        val ownedDependencies = dependencies.filter { it.owners.isNotEmpty() }

        val service = ApiServiceProvider(
            baseUrl = dumpInfoConfig.baseUploadUrl.get(),
            ownerAdapterFactory = OwnerAdapterFactory(ownerSerializer),
            loggerFactory = loggerFactory
        ).provide<UploadDependenciesApi>()
        service.dumpModules(UploadDependenciesRequestBody(DumpInfo.fromExtension(dumpInfoConfig), ownedDependencies))
            .executeWithHttpFailure(errorMessage = "Upload dependencies request failed")
    }
}
