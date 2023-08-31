package com.avito.android.module_type.validation.configurations

import org.gradle.api.Project

internal interface ValidationConfiguration {

    fun configureRoot(project: Project)

    fun configureModule(project: Project)
}
