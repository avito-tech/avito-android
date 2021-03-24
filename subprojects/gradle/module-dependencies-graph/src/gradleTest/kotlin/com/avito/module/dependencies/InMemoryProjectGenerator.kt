package com.avito.module.dependencies

import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.testfixtures.ProjectBuilder

internal fun rootProject(): Project =
    ProjectBuilder.builder()
        .withName("root")
        .build()

internal fun androidApp(name: String, parent: Project): Project {
    val project = buildProject(name, parent)
    project.plugins.apply("com.android.application")
    project.repositories.run {
        add(mavenCentral())
        add(google())
    }
    return project
}

internal fun androidLib(name: String, parent: Project): Project {
    val project = buildProject(name, parent)
    project.plugins.apply("com.android.library")
    project.repositories.run {
        add(mavenCentral())
        add(google())
    }
    return project
}

internal fun javaLib(name: String, parent: Project): Project {
    val project = buildProject(name, parent)
    project.plugins.apply(JavaLibraryPlugin::class.java)
    return project
}

internal fun Project.apiDependency(dependency: Project) {
    dependency("api", dependency)
}

internal fun Project.implementationDependency(dependency: Project) {
    dependency("implementation", dependency)
}

private fun Project.dependency(configuration: String, dependency: Project) {
    with(dependencies) {
        add(configuration, project(mapOf("path" to ":${dependency.path}")))
    }
}

private fun buildProject(name: String, parent: Project): Project =
    ProjectBuilder.builder()
        .withName(name)
        .withParent(parent)
        .build()
