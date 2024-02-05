package com.avito.android.info

import com.avito.android.CodeOwnershipExtension
import com.avito.android.OwnerSerializerProvider
import com.avito.android.model.Owner
import com.avito.android.owner.adapter.OwnerAdapterFactory
import com.avito.android.owner.dependency.JsonOwnedDependenciesSerializer
import com.avito.android.owner.dependency.OwnedDependency
import com.avito.android.serializers.OwnerNameSerializer
import com.avito.module.metrics.CollectModuleBetweennessCentralityAction
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.findByType

public abstract class ExportInternalDepsCodeOwners : DefaultTask() {

    @get:Internal
    public abstract val ownerSerializer: Property<OwnerSerializerProvider>

    @get:OutputFile
    public abstract val outputFile: RegularFileProperty

    @TaskAction
    public fun printOwnership() {
        val dependencies = extractOwnedDependencies()
        saveOwnedDependencies(dependencies)
    }

    private fun extractOwnedDependencies(): List<OwnedDependency> {
        val dependencies = mutableListOf<OwnedDependency>()
        val betweennessCentrality = CollectModuleBetweennessCentralityAction().collect(project)
        project.subprojects { subproject ->
            dependencies += extractOwnedDependency(subproject, betweennessCentrality)
        }
        return dependencies
    }

    private fun saveOwnedDependencies(dependencies: List<OwnedDependency>) {
        val ownerSerializer = ownerSerializer.orNull?.provideNameSerializer() ?: ToStringOwnerSerializer()
        val dependencySerializer = JsonOwnedDependenciesSerializer(OwnerAdapterFactory(ownerSerializer))
        val output = outputFile.get().asFile
        output.writeText(dependencySerializer.serialize(dependencies))
    }

    private fun extractOwnedDependency(project: Project, betweennessCentrality: Map<Project, Double>): OwnedDependency {
        val extension = project.extensions.findByType<CodeOwnershipExtension>()
        val owners = extension?.owners?.orNull ?: emptySet()
        return OwnedDependency(
            name = project.path,
            owners = owners,
            type = OwnedDependency.Type.INTERNAL,
            betweennessCentrality = betweennessCentrality.getOrDefault(project, null)
        )
    }

    private class ToStringOwnerSerializer : OwnerNameSerializer {

        override fun deserialize(ownerName: String): Owner {
            error("Can't parse $ownerName to owner entity. This operation is unsupported in ToStringOwnersSerializer")
        }

        override fun serialize(owner: Owner): String {
            return owner.toString()
        }
    }

    public companion object {
        public const val NAME: String = "exportInternalDepsCodeOwners"
    }
}
