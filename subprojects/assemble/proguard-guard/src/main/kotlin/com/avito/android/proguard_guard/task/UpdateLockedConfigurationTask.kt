package com.avito.android.proguard_guard.task

import com.avito.android.proguard_guard.configuration.parseConfigurationSorted
import com.avito.android.proguard_guard.configuration.print
import com.avito.android.proguard_guard.configuration.writeTo
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@CacheableTask
public abstract class UpdateLockedConfigurationTask @Inject constructor(
    private val debug: Boolean,
) : ProguardGuardTask() {

    @get:OutputFile
    public abstract val lockedConfigurationFile: RegularFileProperty

    @get:OutputFile
    public abstract val sortedMergedConfigurationFile: RegularFileProperty

    @TaskAction
    public fun update() {
        val sortedMergedConfigurationFile = sortedMergedConfigurationFile.get().asFile
        val lockedFile = lockedConfigurationFile.get().asFile

        val configuration = parseConfigurationSorted(mergedConfigurationFile.get().asFile)
        if (debug) {
            configuration.print(this)
        }
        configuration.writeTo(sortedMergedConfigurationFile)

        sortedMergedConfigurationFile.copyTo(lockedFile, overwrite = true)

        logger.lifecycle("Locked proguard configuration has been updated: $lockedFile")
    }
}
