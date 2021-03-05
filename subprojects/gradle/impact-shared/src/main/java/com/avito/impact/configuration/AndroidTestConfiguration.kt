@file:Suppress("DEPRECATION")
// todo use new api?

package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.impact.configuration.sets.isAndroidTest
import com.avito.module.configurations.ConfigurationType.AndroidTests

class AndroidTestConfiguration(module: InternalModule) :
    BaseConfiguration(module, setOf(AndroidTests::class.java)) {

    override val isModified: Boolean by lazy {
        dependencies.any { it.isModified }
            || module.mainConfiguration.isModified
            || hasChangedFiles
    }

    override fun containsSources(sourceSet: AndroidSourceSet) = sourceSet.isAndroidTest()

    override fun toString(): String {
        return "AndroidTestConfiguration(${project.path})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AndroidTestConfiguration

        if (project != other.project) return false

        return true
    }

    override fun hashCode(): Int {
        return project.hashCode()
    }
}
