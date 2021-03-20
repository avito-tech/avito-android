package com.avito.module.internal.dependencies

import org.gradle.api.Project

internal data class ProjectWithDeps(
    val project: Project,
    val dependencies: Set<Project>
)
