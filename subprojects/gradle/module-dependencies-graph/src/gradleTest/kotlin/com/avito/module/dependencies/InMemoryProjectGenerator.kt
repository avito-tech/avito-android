package com.avito.module.dependencies

import org.gradle.api.Project
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

private fun buildProject(name: String, parent: Project): Project =
    ProjectBuilder.builder()
        .withName(name)
        .withParent(parent)
        .build()
