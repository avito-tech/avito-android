package com.avito.impact

import com.avito.impact.configuration.internalModule
import org.gradle.api.Project
import java.io.File

class BytecodeResolver(
    private val project: Project
) {
    fun resolveBytecode(configurationType: ConfigurationType): Set<File> =
        project.internalModule.getConfiguration(configurationType).fullBytecodeSets

    fun resolveBytecodeWithoutDependencyToAnotherConfigurations(configurationType: ConfigurationType): Set<File> =
        project.internalModule.getConfiguration(configurationType).let { configuration ->
            configuration.bytecodeSets() + configuration.dependencies.flatMap { it.fullBytecodeSets }
        }
}
