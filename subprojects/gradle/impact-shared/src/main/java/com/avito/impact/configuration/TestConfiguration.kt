@file:Suppress("DEPRECATION")
//todo use new api?

package com.avito.impact.configuration

import com.android.build.gradle.api.AndroidSourceSet
import com.avito.impact.configuration.sets.isTest
import java.io.File

class TestConfiguration(module: InternalModule) : SimpleConfiguration(module) {

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
        dependencies { it.isTest() }
    }

    override fun containsSources(sourceSet: AndroidSourceSet) = sourceSet.isTest()
    override fun containsBytecode(bytecodeDirectory: File): Boolean = bytecodeDirectory.isTest()

    override fun toString(): String {
        return "TestConfiguration(${project.path})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TestConfiguration

        if (project != other.project) return false

        return true
    }

    override fun hashCode(): Int {
        return project.hashCode()
    }
}
