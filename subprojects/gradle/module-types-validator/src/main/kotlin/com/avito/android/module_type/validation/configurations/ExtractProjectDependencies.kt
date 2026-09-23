package com.avito.android.module_type.validation.configurations

import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.HasAndroidTest
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.register

internal fun Project.registerExtractProjectDependencies(
    taskName: String,
    reportFile: Provider<RegularFile>,
    classpath: DependencyClasspath,
): TaskProvider<ExtractProjectDependenciesTask> {
    val extractTask = tasks.register<ExtractProjectDependenciesTask>(taskName) {
        output.set(reportFile)
    }

    fun addClasspaths(compile: () -> Configuration, runtime: () -> Configuration) {
        extractTask.configure { task ->
            val configuration = when (classpath) {
                DependencyClasspath.COMPILE -> compile()
                DependencyClasspath.RUNTIME -> runtime()
            }
            task.roots.put(configuration.name, configuration.incoming.resolutionResult.rootComponent)
        }
    }

    plugins.withId("java") {
        val sourceSets = extensions.getByType(SourceSetContainer::class.java)
        val main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
        addClasspaths(
            compile = { configurations.getByName(main.compileClasspathConfigurationName) },
            runtime = { configurations.getByName(main.runtimeClasspathConfigurationName) },
        )
    }
    plugins.withId("com.android.base") {
        extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
            addClasspaths(
                compile = { variant.compileConfiguration },
                runtime = { variant.runtimeConfiguration },
            )
            (variant as? HasAndroidTest)?.androidTest?.let { androidTest ->
                addClasspaths(
                    compile = { androidTest.compileConfiguration },
                    runtime = { androidTest.runtimeConfiguration },
                )
            }
        }
    }
    return extractTask
}
