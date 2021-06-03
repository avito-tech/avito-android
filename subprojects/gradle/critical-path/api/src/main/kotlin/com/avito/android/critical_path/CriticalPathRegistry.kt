package com.avito.android.critical_path

import com.avito.android.critical_path.internal.PathBuildProvider
import com.avito.android.gradle.metric.GradleCollector
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Project

public object CriticalPathRegistry {

    public fun addListener(project: Project, listener: CriticalPathListener) {
        check(project.isRoot()) {
            "Can consume build critical path only in root project but was ${project.path}"
        }
        getProvider(project).addPathListener(listener)
    }

    private fun getProvider(project: Project): PathBuildProvider {
        return if (project.extensions.extraProperties.has(listenerExtra)) {
            project.extensions.extraProperties.get(listenerExtra) as PathBuildProvider
        } else {
            val buildListener = PathBuildProvider()

            GradleCollector.initialize(project, listOf(buildListener))

            project.extensions.extraProperties.set(listenerExtra, buildListener)

            buildListener
        }
    }
}

private val listenerExtra: String = CriticalPathListener::class.java.name.toString()
