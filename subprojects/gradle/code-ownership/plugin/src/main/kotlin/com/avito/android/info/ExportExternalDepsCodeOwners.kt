package com.avito.android.info

import com.avito.android.OwnerSerializerProvider
import com.avito.android.check.deps.ExternalDepsCodeOwnersChecker.Companion.DEPENDENCIES_SECTION_NAMES
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.owner.dependency.JsonOwnedDependenciesSerializer
import com.avito.android.owner.dependency.OwnedDependency
import com.avito.android.serializers.OwnerNameSerializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.toml.TomlMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class ExportExternalDepsCodeOwners : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val libsOwnersFile: RegularFileProperty

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val libsVersionsFile: RegularFileProperty

    @get:Internal
    public abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:OutputFile
    public abstract val outputFile: RegularFileProperty

    @TaskAction
    public fun printOwnership() {
        val ownerSerializer = ownerSerializer.get().provideNameSerializer()
        val dependencies = extractOwnedDependencies(ownerSerializer)
        saveOwnedDependencies(dependencies, ownerSerializer)
    }

    private fun extractOwnedDependencies(ownerSerializer: OwnerNameSerializer): List<OwnedDependency> {
        val versionsFile = libsVersionsFile.get().asFile
        val ownersFile = libsOwnersFile.get().asFile

        val mapper = TomlMapper()
        val versionsFileData = mapper.readTree(versionsFile)
        val ownersFileData = mapper.readTree(ownersFile)

        return DEPENDENCIES_SECTION_NAMES.flatMap { sectionName ->
            extractOwnedDependencies(versionsFileData[sectionName], ownersFileData[sectionName], ownerSerializer)
        }
    }

    private fun extractOwnedDependencies(
        versionsFileSection: JsonNode,
        ownersFileSection: JsonNode,
        ownerSerializer: OwnerNameSerializer
    ): List<OwnedDependency> {
        val dependencies = mutableListOf<OwnedDependency>()
        versionsFileSection.fieldNames().forEach { dependencyName ->
            val versionsFileEntry = versionsFileSection[dependencyName]
            val fullDependencyName: String? = when {
                versionsFileEntry.isTextual -> versionsFileEntry.textValue().substringBeforeLast(':')
                versionsFileEntry.has("id") -> versionsFileEntry["id"].textValue()
                else -> versionsFileEntry["module"].textValue()
            }
            val owner = ownersFileSection[dependencyName].textValue()

            dependencies.add(
                OwnedDependency(
                    name = fullDependencyName ?: dependencyName,
                    owners = listOf(ownerSerializer.deserialize(owner)),
                    type = OwnedDependency.Type.EXTERNAL
                )
            )
        }
        return dependencies
    }

    private fun saveOwnedDependencies(dependencies: List<OwnedDependency>, ownerSerializer: OwnerNameSerializer) {
        val dependencySerializer = JsonOwnedDependenciesSerializer(OwnerAdapterFactory(ownerSerializer))
        val output = outputFile.get().asFile
        output.writeText(dependencySerializer.serialize(dependencies))
    }

    public companion object {
        public const val NAME: String = "exportExternalDepsCodeOwners"
    }
}
