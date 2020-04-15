package com.avito.android.plugin.build_param_check

import com.avito.android.isAndroid
import com.avito.android.isAndroidApp
import com.avito.impact.configuration.internalModule
import com.avito.impact.util.AndroidManifest
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class UniqueRClassesTask : DefaultTask() {

    @TaskAction
    fun check() {
        val apps = project.subprojects.filter { it.isAndroidApp() }

        apps.forEach { app ->
            val packages: List<String> = app.internalModule
                .implementationConfiguration.allDependencies()
                .filter { it.module.project.isAndroid() }
                .map { AndroidManifest.from(it.module.project).getPackage() }

            val duplicates = packages.duplicates()
            if (duplicates.isNotEmpty()) {
                throw IllegalStateException("Application ${app.path} has modules with the same package: $duplicates.\n" +
                    "It leads to unexpected resource overriding.\n" +
                    "Please, make packages unique.")
            }
        }
    }

    private fun <T> List<T>.duplicates(): Set<T> {
        val uniques = mutableSetOf<T>()
        return this.filter { !uniques.add(it) }.toSet()
    }
}
