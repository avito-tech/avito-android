package com.avito.android.proguard_guard.task

import com.avito.android.diff_util.EditList
import com.avito.android.proguard_guard.diff.ConfigurationDiffBuilder
import com.avito.android.proguard_guard.diff.EditListFormatter
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.lang.IllegalStateException

@CacheableTask
public abstract class CheckMergedConfigurationTask : DefaultTask() {

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val mergedConfigurationFile: RegularFileProperty

    // Cannot use @InputFile because it could not exist (https://github.com/gradle/gradle/issues/2016)
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val lockedConfigurationFile: RegularFileProperty

    @get:Input
    public abstract val failOnDifference: Property<Boolean>

    @get:OutputFile
    public abstract val diffFile: RegularFileProperty

    @TaskAction
    public fun check() {
        val diffFile = diffFile.get().asFile.also {
            it.delete()
        }

        val lockedConfigurationFile = lockedConfigurationFile.get().asFile
        val mergedConfigurationFile = mergedConfigurationFile.get().asFile

        logger.lifecycle("Locked proguard config: $lockedConfigurationFile")
        logger.lifecycle("Merged proguard config: $mergedConfigurationFile")

        if (lockedConfigurationFile.exists()) {
            compareConfigurations(
                lockedConfigurationFile = lockedConfigurationFile,
                mergedConfigurationFile = mergedConfigurationFile,
                diffFile = diffFile,
            )
        } else {
            logger.lifecycle("Locked proguard config does not exist. Just creating it.")
            mergedConfigurationFile.copyTo(lockedConfigurationFile)
        }
    }

    private fun compareConfigurations(
        lockedConfigurationFile: File,
        mergedConfigurationFile: File,
        diffFile: File
    ) {
        val lockedConfigurationLines = lockedConfigurationFile.readMeaningfulLines()
        val mergedConfigurationLines = mergedConfigurationFile.readMeaningfulLines()

        val editList: EditList = ConfigurationDiffBuilder().build(
            lockedConfigurationLines,
            mergedConfigurationLines
        )

        if (editList.isEmpty()) {
            logger.lifecycle("Proguard configurations are the same")
        } else {
            val diff = EditListFormatter(
                lockedConfigurationLines,
                mergedConfigurationLines
            ).format(editList)

            diffFile.writeText(diff)

            val errorText = "Merged proguard configuration has changed. See diff at ${diffFile.path}:\n$diff"
            if (failOnDifference.get()) {
                throw IllegalStateException(errorText)
            } else {
                logger.warn(errorText)
            }
        }
    }

    private fun File.readMeaningfulLines(): List<String> {
        return readLines().filterNot {
            it.isBlank() || it.startsWith('#')
        }
    }
}
