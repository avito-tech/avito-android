package com.avito.android.plugin

import com.avito.android.isAndroidLibrary
import com.avito.impact.configuration.internalModule
import com.avito.impact.util.AndroidManifest
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File
import javax.inject.Inject

abstract class FixNamespacedResourcesTask @Inject constructor(
    objects: ObjectFactory
) : DefaultTask() {

    @Suppress("UnstableApiUsage")
    @InputFile
    val file: RegularFileProperty = objects.fileProperty()

    @Optional
    @Input
    val modulePath = objects.property<String>()

    @TaskAction
    fun check() {
        require(modulePath.isPresent) {
            "Can't find an Android application module with $FILES_PREFIX_PROPERTY=${file.get()} files inside"
        }

        val app: Project = requireNotNull(
            project.findProject(modulePath.get())
        )
        val appModule = getApplicationModule(app)
        val libraries: List<AndroidModule> = findLibraries(app)
        fixFiles(appModule, libraries)
    }

    private fun getApplicationModule(app: Project): AndroidModule {
        val packageId = AndroidManifest.from(app).getPackage()
        return AndroidModule(packageId, ids = emptyList())
    }

    private fun findLibraries(appModule: Project): List<AndroidModule> {
        return appModule.internalModule
            .configurations
            .flatMap { it.allDependencies() }
            .asSequence()
            .filter { it.module.project.isAndroidLibrary() }
            .map { it.module.project }
            .toSet()
            .map { project ->
                val resourceIds = parseResourceIds(project)
                val packageId = AndroidManifest.from(project).getPackage()
                AndroidModule(packageId, resourceIds)
            }
            .toList()
    }

    private fun fixFiles(app: AndroidModule, libraries: List<AndroidModule>) {
        val file: File = file.get().asFile
        val files: Set<File> = if (file.isDirectory) {
            file.walk()
                .filter { it.extension == "kt" }
                .toSet()
        } else {
            setOf(file)
        }
        val fixer = FileResourcesFixer()
        files.forEach { file ->
            val newContent = fixer.fixMergedResources(file, app, libraries)
            if (newContent != null) {
                file.writeText(newContent)
            }
        }
    }

    private fun parseResourceIds(module: Project): List<String> {
        val rFile = File(
            module.buildDir,
            "intermediates/compile_symbol_list/release/R.txt" // TODO: use public artifacts
        )
        require(rFile.exists()) {
            "Can't find $rFile"
        }

        return rFile.readLines() // format: int id current_price 0x0
            .filter { it.startsWith("int id ") && it.endsWith(" 0x0") }
            .map { it.split(' ')[2] }
            .toList()
    }
}


internal data class AndroidModule(val packageId: String, val ids: List<String>)

internal data class ResourceId(val id: String, val module: AndroidModule)

