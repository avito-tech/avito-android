package com.avito.android.check.deps

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

public abstract class CheckInternalDepsTask : DefaultTask() {

    @get:Internal
    public abstract val projectDir: DirectoryProperty

    @TaskAction
    public fun checkDescription() {
        val logicalModuleDir = projectDir.get().asFile.parentFile
        require(logicalModuleDir.resolve("README.md").exists()) {
            "Logical module ${logicalModuleDir.path}" +
                " should have a README.md file with a description of the module's purpose"
        }
    }

    public companion object {
        public const val NAME: String = "checkInternalDeps"
    }
}
