package com.avito.android

import org.apache.commons.configuration.PropertiesConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.wrapper.Wrapper
import java.util.Locale

abstract class CheckWrapper : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val expectedGradleVersion: Property<String>

    @get:Input
    @get:Optional
    abstract val expectedDistributionType: Property<Wrapper.DistributionType>

    @get:Input
    @get:Optional
    abstract val expectedDistributionUrl: Property<String>

    @get:InputFiles
    abstract val wrapperPropertiesFiles: SetProperty<RegularFile>

    @TaskAction
    fun doWork() {
        val expectedGradleVersion = expectedGradleVersion.orNull
        val expectedDistributionType = expectedDistributionType.orNull?.name?.toLowerCase(Locale.getDefault())
        val expectedDistributionUrl = expectedDistributionUrl.orNull

        val inconsistencies = mutableListOf<Inconsistency>()

        wrapperPropertiesFiles.get().map { it.asFile }.forEach { wrapperPropertiesFile ->

            val filePath = wrapperPropertiesFile.toRelativeString(project.projectDir)

            val wrapperProperties = PropertiesConfiguration(wrapperPropertiesFile)
            val distributionUrl = wrapperProperties.getString("distributionUrl", "")

            if (!wrapperPropertiesFile.exists()) {
                inconsistencies.add(
                    Inconsistency(
                        file = filePath,
                        reason = "file doesn't exists"
                    )
                )
            } else {

                if (expectedGradleVersion != null) {
                    if (!distributionUrl.contains(expectedGradleVersion)) {
                        inconsistencies.add(
                            Inconsistency(
                                file = filePath,
                                reason = "project gradle version is '$expectedGradleVersion', " +
                                    "but ${project.relativePath(wrapperPropertiesFile)} " +
                                    "points to another version: $distributionUrl"
                            )
                        )
                    }
                }

                if (!expectedDistributionType.isNullOrBlank()) {
                    if (!distributionUrl.contains("-$expectedDistributionType.")) {
                        inconsistencies.add(
                            Inconsistency(
                                file = filePath,
                                reason = "gradle distribution type is '$expectedDistributionType', " +
                                    "but ${project.relativePath(wrapperPropertiesFile)} " +
                                    "specifies another type: $distributionUrl"
                            )
                        )
                    }
                }

                if (!expectedDistributionUrl.isNullOrBlank()) {
                    if (distributionUrl != expectedDistributionUrl) {
                        inconsistencies.add(
                            Inconsistency(
                                file = filePath,
                                reason = "Expected gradle distribution url is '$expectedDistributionUrl', " +
                                    "but ${project.relativePath(wrapperPropertiesFile)} " +
                                    "specifies different one: $distributionUrl"
                            )
                        )
                    }
                }
            }
        }

        if (inconsistencies.isNotEmpty()) {
            throw IllegalStateException(errorMessage(inconsistencies))
        }
    }

    private fun errorMessage(inconsistencies: List<Inconsistency>): String {
        return buildString {
            appendLine("Gradle wrappers inconsistency found")
            appendLine("Run :wrapper task to fix it")

            val map = inconsistencies.groupBy { it.file }

            map.keys.forEach { file: String ->
                appendLine(" - file: $file")
                map[file]?.forEach { inconsistency: Inconsistency ->
                    appendLine("   - ${inconsistency.reason}")
                }
            }
        }
    }
}
