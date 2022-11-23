package com.avito.android.tech_budget.internal.owners.dependencies

import com.avito.android.OwnerSerializer
import com.avito.android.owner.dependency.JsonOwnedDependenciesSerializer
import com.avito.android.owner.dependency.OwnedDependency
import com.avito.android.tech_budget.DumpInfoConfiguration
import com.avito.android.tech_budget.internal.dump.DumpInfo
import com.avito.android.tech_budget.internal.owners.dependencies.models.UploadDependenciesRequestBody
import com.avito.android.tech_budget.internal.utils.executeWithHttpFailure
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
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
    abstract val ownerSerializer: Property<OwnerSerializer>

    @get:Nested
    abstract val dumpInfoConfiguration: Property<DumpInfoConfiguration>

    @TaskAction
    fun uploadDependencies() {
        val internalDeps = extractDependencies(internalDependencies.asFile.get())
        val externalDeps = extractDependencies(externalDependencies.asFile.get())
        uploadDependencies(internalDeps + externalDeps)
    }

    private fun extractDependencies(file: File): List<OwnedDependency> {
        val ownedDependenciesSerializer = JsonOwnedDependenciesSerializer(ownerSerializer.get())
        val rawOwners = file.readText()
        return ownedDependenciesSerializer.deserialize(rawOwners)
    }

    private fun uploadDependencies(dependencies: List<OwnedDependency>) {
        val dumpInfoConfig = dumpInfoConfiguration.get()

        val service = UploadDependenciesApi.create(
            baseUrl = dumpInfoConfig.baseUploadUrl.get(),
            ownerSerializer = ownerSerializer.get()
        )
        service.dumpModules(UploadDependenciesRequestBody(DumpInfo.fromExtension(dumpInfoConfig), dependencies))
            .executeWithHttpFailure(errorMessage = "Upload dependencies request failed")
    }
}
