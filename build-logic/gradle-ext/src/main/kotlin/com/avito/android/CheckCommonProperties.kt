package com.avito.android

import org.apache.commons.configuration.PropertiesConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction

abstract class CheckCommonProperties : DefaultTask() {

    @get:InputFile
    abstract val commonPropertiesFile: RegularFileProperty

    @get:InputFiles
    abstract val gradlePropertiesFiles: SetProperty<RegularFile>

    @TaskAction
    fun doWork() {
        val commonProperties = PropertiesConfiguration(commonPropertiesFile.get().asFile)

        val inconsistencies = mutableListOf<Inconsistency>()

        gradlePropertiesFiles.get().map { it.asFile }.forEach { gradlePropertiesFile ->

            val properties = if (gradlePropertiesFile.exists()) {
                PropertiesConfiguration(gradlePropertiesFile)
            } else {
                inconsistencies.add(
                    Inconsistency(
                        file = gradlePropertiesFile.path,
                        reason = "File not found"
                    )
                )
                return@forEach
            }

            commonProperties.keys.forEach { key ->
                if (!properties.containsKey(key)) {
                    inconsistencies.add(
                        Inconsistency(
                            file = gradlePropertiesFile.path,
                            reason = "missing property: $key"
                        )
                    )
                } else {
                    val commonValue = commonProperties.getProperty(key)
                    val value = properties.getProperty(key)
                    if (value != commonValue) {
                        inconsistencies.add(
                            Inconsistency(
                                file = gradlePropertiesFile.path,
                                reason = "different value for property: $key\n" +
                                    "     expected: $commonValue\n" +
                                    "     actual  : $value"
                            )
                        )
                    }

                    val commonComment = commonProperties.getRawComment(key)
                    val comment = properties.getRawComment(key)

                    val expectedComment = generateComment(commonComment)
                    if (comment != expectedComment) {
                        inconsistencies.add(
                            Inconsistency(
                                gradlePropertiesFile.path,
                                "different comments for property: $key;\n" +
                                    "     expected: $expectedComment\n" +
                                    "     actual  : $comment"
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
            appendLine("Common gradle properties inconsistency found")
            appendLine("Run :generateCommonProperties task to fix it")

            val map = inconsistencies.groupBy { it.file }

            map.keys.forEach { project: String ->
                appendLine(" - project: $project")
                map[project]?.forEach { inconsistency: Inconsistency ->
                    appendLine("   - ${inconsistency.reason}")
                }
            }
        }
    }
}
