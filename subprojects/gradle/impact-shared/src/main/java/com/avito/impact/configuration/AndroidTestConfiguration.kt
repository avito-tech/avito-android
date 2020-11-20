@file:Suppress("DEPRECATION")
// todo use new api?

package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.impact.configuration.sets.isAndroidTest
import java.io.File

class AndroidTestConfiguration(module: InternalModule) : SimpleConfiguration(module) {

    override val isModified: Boolean by lazy {
        dependencies.any { it.isModified }
            || module.implementationConfiguration.isModified
            || hasChangedFiles
    }

    override val fullBytecodeSets: Set<File> by lazy {
        bytecodeSets() +
            dependencies.flatMap { it.fullBytecodeSets } +
            module.implementationConfiguration.fullBytecodeSets
    }

    override val dependencies: Set<ImplementationConfiguration> by lazy {
        dependencies { it.isAndroidTest() }
    }

    override fun containsSources(sourceSet: AndroidSourceSet) = sourceSet.isAndroidTest()
    override fun containsBytecode(bytecodeDirectory: File): Boolean = bytecodeDirectory.isAndroidTest()

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
