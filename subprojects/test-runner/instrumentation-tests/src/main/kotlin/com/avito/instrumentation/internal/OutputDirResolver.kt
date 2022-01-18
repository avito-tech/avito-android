package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import org.gradle.api.file.Directory
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ProviderFactory
import org.slf4j.Logger

internal class OutputDirResolver(
    private val extension: InstrumentationTestsPluginExtension,
    private val rootProjectLayout: ProjectLayout,
    private val providers: ProviderFactory,
    private val logger: Logger
) {

    fun resolveWithDeprecatedProperty(): Provider<Directory> {
        return extension.outputDir.map {
            getDeprecatedOutputValueIfSet().orNull ?: it
        }
    }

    private fun getDeprecatedOutputValueIfSet(): Provider<Directory> {
        @Suppress("DEPRECATION")
        return providers.provider {
            if (extension.output != extension.defaultOutput.get().asFile.path) {
                logger.warn(
                    "Deprecated 'output' property used in InstrumentationTestsPluginExtension, " +
                        "please migrate to 'outputDir'"
                )

                val rootDirPath = rootProjectLayout.projectDirectory.asFile.path
                if (extension.output.startsWith(rootDirPath)) {
                    val relativePath = extension.output.replace(rootDirPath, "").removePrefix("/")
                    rootProjectLayout.projectDirectory.dir(relativePath)
                } else {
                    logger.warn(
                        "Deprecated 'output' property used in InstrumentationTestsPluginExtension " +
                            "has incorrect input: should be inside project root directory, but was: ${extension.output}"
                    )
                    null
                }
            } else {
                null
            }
        }
    }
}
