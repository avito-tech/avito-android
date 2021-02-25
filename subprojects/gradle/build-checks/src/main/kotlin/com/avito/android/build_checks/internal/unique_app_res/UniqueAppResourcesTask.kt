package com.avito.android.build_checks.internal.unique_app_res

import com.android.ide.common.symbols.SymbolIo
import com.android.ide.common.symbols.SymbolTable
import com.android.resources.ResourceType
import com.avito.android.build_checks.AndroidAppChecksExtension
import com.avito.android.build_checks.internal.FailedCheckMessage
import com.avito.android.build_checks.internal.docsLink
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.nio.file.Path
import javax.inject.Inject

@CacheableTask
public abstract class UniqueAppResourcesTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val packageAwareRFiles: Property<FileCollection>

    @Input
    public val ignoredResourceTypes: ListProperty<String> = objects.listProperty(String::class.java)

    @get:Input
    internal val ignoredResources: SetProperty<Resource> = objects.setProperty(Resource::class.java)

    private val symbolTables by lazy {
        packageAwareRFiles.get().files
            .map {
                parsePackageAwareR(it.toPath())
            }
    }

    @OutputFile
    public val output: Property<RegularFile> = objects.fileProperty().apply {
        set(project.layout.buildDirectory.file("${UniqueAppResourcesTask::class.java.simpleName}.output"))
    }

    @TaskAction
    public fun check() {
        warnUndeclaredIgnoredResources()
        requireNoDuplicates()

        output.get().asFile.writeText("no duplicates")
    }

    private fun requireNoDuplicates() {
        val duplicates = duplicatesFinder().findDuplicates()

        if (duplicates.isNotEmpty()) {
            val duplicatesMessage = duplicates.joinToString(separator = "\n") { duplicate ->
                "- ${duplicate.resource.type.getName()} \'${duplicate.resource.name}\' " +
                    "in packages: ${duplicate.packages}"
            }
            @Suppress("MaxLineLength")
            throw IllegalStateException(
                FailedCheckMessage(
                    AndroidAppChecksExtension::uniqueAppResources,
                    """
                    |Application ${project.path} has modules with the same resource names:
                    |
                    |$duplicatesMessage
                    |
                    |This can lead to overriding.
                    |See "Resource merge conflicts" in https://developer.android.com/studio/projects/android-library#Considerations
                    |To avoid resource conflicts, consider using a prefix (`android.resourcePrefix`) or other consistent naming scheme.
                    |You can add these resource to exceptions in buildChecks block
                    """.trimMargin()
                ).toString()
            )
        }
    }

    private fun warnUndeclaredIgnoredResources() {
        val resources = undeclaredResourcesFinder().findResources()

        if (resources.isNotEmpty()) {
            val resourcesMessage = resources.joinToString(separator = "\n") { res ->
                "- ${res.type.getName()} \'${res.name}\' "
            }
            @Suppress("MaxLineLength")
            logger.warn(
                """Application ${project.path} defines unknown resources in '${AndroidAppChecksExtension::uniqueAppResources.name}':
                    $resourcesMessage
                    Remove them.
                    See $docsLink for details.
                    """
            )
        }
    }

    private fun duplicatesFinder(): DuplicateResourcesFinder {
        val ignoredTypes: Set<ResourceType> = ignoredResourceTypes.get()
            .map { ResourceType.fromClassName(it) }
            .toSet()

        return DuplicateResourcesFinderImpl(
            symbolTables,
            ignoredResourceTypes = unsupportedTypes + ignoredTypes,
            ignoredResources.get()
        )
    }

    private fun undeclaredResourcesFinder(): UndeclaredResourcesFinder =
        UndeclaredResourcesFinderImpl(
            symbolTables,
            ignoredResources.get()
        )
}

internal fun parsePackageAwareR(path: Path): SymbolTable =
    SymbolIo().readSymbolListWithPackageName(path)

private val unsupportedTypes = setOf(
    ResourceType.ID, // can be the same in different layouts
    ResourceType.STYLEABLE, // can't reproduce an overriding
    ResourceType.ATTR, // can be the same in different styles
)
