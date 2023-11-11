package com.avito.instrumentation.internal

import com.avito.instrumentation.configuration.InstrumentationTestsPluginExtension
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider

internal class OutputDirResolver(
    private val extension: InstrumentationTestsPluginExtension,
) {

    fun resolveWithDeprecatedProperty(): Provider<Directory> {
        return extension.outputDir
    }
}
