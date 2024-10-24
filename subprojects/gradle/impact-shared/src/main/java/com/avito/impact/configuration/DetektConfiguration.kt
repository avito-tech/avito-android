package com.avito.impact.configuration

import com.avito.module.configurations.ConfigurationType.Detekt

internal class DetektConfiguration(module: InternalModule) : BaseConfiguration(module, Detekt) {

    override val isModified: Boolean by lazy {
        dependencies.any { it.isModified }
            || module.androidTestConfiguration.isModified
            || hasChangedFiles
    }

    override fun containsSources(
        @Suppress("DEPRECATION")
        sourceSet: com.android.build.gradle.api.AndroidSourceSet
    ) = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DetektConfiguration
        if (project != other.project) return false
        return true
    }

    override fun hashCode(): Int {
        return project.hashCode()
    }
}
