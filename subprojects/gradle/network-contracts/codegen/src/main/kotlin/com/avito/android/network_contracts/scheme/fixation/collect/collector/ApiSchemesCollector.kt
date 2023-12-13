package com.avito.android.network_contracts.scheme.fixation.collect.collector

import java.io.File

internal class ApiSchemesCollector(
    private val projectPath: String,
) {

    fun collect(codegenTomlFile: File, schemesDirectory: File): Map<String, File> {
        if (!codegenTomlFile.exists()) {
            return emptyMap()
        }

        return buildMap {
            val schemes = schemesDirectory.collectSchemes()

            schemes.forEach { schemaFile ->
                val schemePath = schemaFile.toRelativeString(schemesDirectory)
                val depsPath = "deps/${prepareProjectPathAsDependency(projectPath)}/$schemePath"
                put(depsPath, schemaFile)
            }
        }
    }

    private fun prepareProjectPathAsDependency(path: String): String {
        return path.removePrefix(":")
            .replace(":", "_")
    }
}

private fun File.collectSchemes(): Sequence<File> {
    return walk().filter { it.extension == "yaml" }
}
