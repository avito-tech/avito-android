package com.avito.android.module_type.internal

import java.io.File
import java.nio.file.Path

internal class ModuleDescriptionReader(
    private val rootDir: File
) {

    /**
     * Workaround for project isolation.
     * We can't consume outputs of [ExtractModuleDescriptionTask] directly
     * through ConfigurableFileCollection or similar input.
     * To configure inputs we need to get providers of [ExtractModuleDescriptionTask] in dependent projects.
     * Referring dependent projects breaks project isolation in configuration phase.
     */
    internal fun read(projectPath: String): ModuleDescription {
        val relativePathToProject = projectPath.removePrefix(":")
            .replace(':', File.separatorChar)

        val pathToDescriptor = Path.of(
            rootDir.path,
            relativePathToProject,
            "build",
            ExtractModuleDescriptionTask.outputPath
        )
        val description = FileSerializer.read(pathToDescriptor.toFile())
        check(description is ModuleDescription) {
            "Expected ${ModuleDescription::class.java} in $pathToDescriptor but was ${description::class.java}"
        }
        return description
    }
}
