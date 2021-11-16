package com.avito.impact

import com.avito.module.configurations.ConfigurationType
import org.gradle.api.Project

public class StubModifiedProjectsFinder : ModifiedProjectsFinder {

    private val projects = mutableSetOf<Project>()
    private val modifiedProjects = mutableMapOf<ConfigurationType, MutableSet<ModifiedProject>>()

    public fun addProjects(vararg project: Project) {
        projects.addAll(project)
    }

    public fun addModifiedProject(project: Project, changeType: ConfigurationType) {
        projects.add(project)
        val modified = modifiedProjects.getOrPut(changeType) { mutableSetOf() }
        modified.add(
            ModifiedProject(project, changedFiles = emptyList())
        )
    }

    override fun allProjects(): Set<Project> {
        return projects
    }

    override fun modifiedProjects(): Set<ModifiedProject> {
        return modifiedProjects.values
            .flatten()
            .toSet()
    }

    override fun modifiedProjects(configurationType: ConfigurationType): Set<ModifiedProject> {
        return modifiedProjects.getOrDefault(configurationType, emptySet())
    }
}
