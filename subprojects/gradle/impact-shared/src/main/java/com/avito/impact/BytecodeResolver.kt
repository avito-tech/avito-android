package com.avito.impact

import com.avito.impact.configuration.internalModule
import org.gradle.api.Project
import java.io.File

class BytecodeResolver(private val project: Project) {

    fun resolveBytecode(reportType: ReportType): Set<File> =
        project.internalModule.getConfiguration(reportType).fullBytecodeSets

    fun resolveBytecodeWithoutDependencyToAnotherConfigurations(reportType: ReportType): Set<File> =
        project.internalModule.getConfiguration(reportType).let { configuration ->
            configuration.bytecodeSets() + configuration.dependencies.flatMap { it.fullBytecodeSets }
        }
}
