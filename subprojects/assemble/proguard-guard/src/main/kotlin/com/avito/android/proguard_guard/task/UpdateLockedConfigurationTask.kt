package com.avito.android.proguard_guard.task

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

@CacheableTask
public abstract class UpdateLockedConfigurationTask : ProguardGuardTask() {

    @get:OutputFile
    public abstract val lockedConfigurationFile: RegularFileProperty

    @TaskAction
    public fun update() {
        val lockedFile = lockedConfigurationFile.get().asFile
        mergedConfigurationFile.get().asFile.copyTo(lockedFile, overwrite = true)
        logger.lifecycle("Locked proguard configuration has been updated: $lockedFile")
    }
}
