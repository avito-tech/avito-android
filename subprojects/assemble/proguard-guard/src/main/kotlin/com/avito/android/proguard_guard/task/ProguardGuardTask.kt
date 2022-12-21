package com.avito.android.proguard_guard.task

import com.android.build.gradle.internal.tasks.R8Task
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskProvider

public abstract class ProguardGuardTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val mergedConfigurationFile: RegularFileProperty
}

/**
 * If [ProguardGuardTask.mergedConfigurationFile] was not set by user, set from r8 task output
 */
internal fun ProguardGuardTask.setMergedFileIfNotPresent(r8TaskProvider: TaskProvider<R8Task>) {
    if (!mergedConfigurationFile.isPresent) {
        mergedConfigurationFile.set(
            r8TaskProvider.flatMap { r8Task ->
                r8Task.getProguardConfigurationOutput()
            }.map { file ->
                RegularFile { file }
            }
        )
    }
}
