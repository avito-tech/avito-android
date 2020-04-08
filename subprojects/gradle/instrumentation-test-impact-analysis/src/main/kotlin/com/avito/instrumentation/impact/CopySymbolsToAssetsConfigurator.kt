package com.avito.instrumentation.impact

import com.android.build.gradle.AppExtension
import com.avito.impact.configuration.internalModule
import com.avito.instrumentation.impact.metadata.ModulePath
import com.avito.instrumentation.impact.metadata.toFileName
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

/**
 * To configure copy task at execution time.
 * From AbstractCopyTask exception:
 * "You cannot add child specs at execution time.
 * Consider configuring this task during configuration time or using a separate task to do the configuration."
 */
abstract class CopySymbolsToAssetsConfigurator : DefaultTask() {

    @Internal
    lateinit var app: AppExtension

    @Internal
    lateinit var targetTask: TaskProvider<Copy>

    @TaskAction
    fun configure() {

        //todo could filter only id's here, to decrease filesize
        val runtimeSymbolList = runtimeSymbolListPath(project.projectDir, app.testBuildType)

        //not available in configuration phase
        val dependencies = project.internalModule.implementationConfiguration.dependencies
        val librarySymbolsFiles = dependencies
            .filter { it.module.project.pluginManager.hasPlugin("com.android.library") }
            .map {
                ModulePath(it.module.path) to symbolListWithPackageNamePath(
                    projectDir = it.module.project.projectDir,
                    variantName = getLibraryBuildVariant(
                        app = app,
                        library = it.module.project,
                        appVariant = app.testBuildType
                    )
                )
            }

        targetTask.configure { copy ->
            copy.from(runtimeSymbolList)
            librarySymbolsFiles.forEach { (modulePath, symbolFile) ->
                copy.from(symbolFile) { spec ->
                    spec.rename { "${modulePath.toFileName()}_$it" }
                }
            }
        }
    }
}
