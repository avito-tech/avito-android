package com.avito.android.build_checks.internal.unique_r

import com.avito.android.build_checks.RootProjectChecksExtension
import com.avito.android.build_checks.internal.FailedCheckMessage
import com.avito.android.isAndroidApp
import com.avito.impact.util.AndroidManifestPackageParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
internal abstract class UniqueRClassesTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val appManifest: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val librariesManifests: Property<FileCollection>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val testManifests: Property<FileCollection>

    @Input
    val allowedNonUniquePackageNames: ListProperty<String> = objects.listProperty(String::class.java)

    @OutputFile
    val output: Property<RegularFile> = objects.fileProperty().apply {
        set(project.layout.buildDirectory.file("${UniqueRClassesTask::class.java.simpleName}.output"))
    }

    @TaskAction
    fun check() {
        check(project.isAndroidApp()) {
            "${project.path} must be an Android application module"
        }

        val packages: List<String> = librariesManifests.get().files
            .asSequence()
            .plus(appManifest.get().asFile)
            .plus(testManifests.get().files)
            .toSet()
            .map {
                requireNotNull(AndroidManifestPackageParser.parse(it))
            }
            .toMutableList()
            .filter { packageName ->
                !allowedNonUniquePackageNames.get().contains(packageName)
            }
            .toList()

        val duplicates = packages.duplicates()
        if (duplicates.isNotEmpty()) {
            throw IllegalStateException(
                FailedCheckMessage(
                    RootProjectChecksExtension::uniqueRClasses,
                    """
                    Application ${project.path} has modules with the same package: $duplicates.
                    It leads to unexpected resource overriding.
                    Please, make packages unique.
                    """
                ).toString()
            )
        }

        output.get().asFile.writeText("no duplicates")
    }

    private fun <T> List<T>.duplicates(): Set<T> {
        val uniques = mutableSetOf<T>()
        return this.filter { !uniques.add(it) }.toSet()
    }
}
