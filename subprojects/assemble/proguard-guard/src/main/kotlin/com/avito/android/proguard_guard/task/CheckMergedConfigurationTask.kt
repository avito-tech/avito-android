package com.avito.android.proguard_guard.task

import com.avito.android.diff_util.EditList
import com.avito.android.proguard_guard.configuration.parseConfigurationSorted
import com.avito.android.proguard_guard.configuration.print
import com.avito.android.proguard_guard.configuration.writeTo
import com.avito.android.proguard_guard.diff.ConfigurationDiffBuilder
import com.avito.android.proguard_guard.diff.EditListFormatter
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import javax.inject.Inject

@CacheableTask
public abstract class CheckMergedConfigurationTask @Inject constructor(
    private val updateTaskPath: String,
    private val debug: Boolean,
) : ProguardGuardTask() {

    // Cannot use @InputFile because it could not exist (https://github.com/gradle/gradle/issues/2016)
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    public abstract val lockedConfigurationFile: RegularFileProperty

    @get:Input
    public abstract val failOnDifference: Property<Boolean>

    @get:OutputFile
    public abstract val diffFile: RegularFileProperty

    @get:OutputFile
    public abstract val sortedMergedConfigurationFile: RegularFileProperty

    @TaskAction
    public fun check() {
        val diffFile = diffFile.get().asFile.also {
            it.delete()
        }
        val sortedMergedConfigurationFile = sortedMergedConfigurationFile.get().asFile.also {
            it.delete()
        }

        val lockedConfigurationFile = lockedConfigurationFile.get().asFile
        val mergedConfigurationFile = mergedConfigurationFile.get().asFile

        logger.lifecycle("Locked proguard config: $lockedConfigurationFile")
        logger.lifecycle("Merged proguard config: $mergedConfigurationFile")

        val sortedConfiguration = parseConfigurationSorted(mergedConfigurationFile)
        if (debug) {
            sortedConfiguration.print(this)
        }
        val sortedConfigurationLines: List<String> = sortedConfiguration.writeTo(sortedMergedConfigurationFile)

        if (lockedConfigurationFile.exists()) {
            compareConfigurations(
                lockedConfigurationLines = lockedConfigurationFile.readLines(),
                mergedConfigurationLines = sortedConfigurationLines,
                diffFile = diffFile,
            )
        } else {
            logger.lifecycle("Locked proguard config does not exist. Just creating it.")
            sortedMergedConfigurationFile.copyTo(lockedConfigurationFile)
        }
    }

    private fun compareConfigurations(
        lockedConfigurationLines: List<String>,
        mergedConfigurationLines: List<String>,
        diffFile: File
    ) {
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

            val errorText = """
                |Merged proguard configuration has changed. 
                |See diff at ${diffFile.path}:
                |
                |$diff
                |Call './gradlew $updateTaskPath' to update locked configuration
            """.trimMargin()
            if (failOnDifference.get()) {
                throw IllegalStateException(errorText)
            } else {
                logger.warn(errorText)
            }
        }
    }
}
