package com.avito.android

import org.apache.commons.configuration.PropertiesConfiguration
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectories
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CheckCommonProperties : DefaultTask() {

    @get:InputFile
    abstract val commonPropertiesFile: RegularFileProperty

    @get:OutputDirectories
    abstract val projectDirs: SetProperty<Directory>

    @TaskAction
    fun doWork() {
        val commonProperties = PropertiesConfiguration(commonPropertiesFile.get().asFile)

        val inconsistencies = mutableListOf<Inconsistency>()

        projectDirs.get().forEach { projectDir ->

            val projectPath = project.projectDir.toRelativeString(projectDir.asFile)

            val gradlePropertiesFile = File(projectDir.asFile, "gradle.properties")

            val properties = if (gradlePropertiesFile.exists()) {
                PropertiesConfiguration(gradlePropertiesFile)
            } else {
                inconsistencies.add(
                    Inconsistency(
                        projectPath = projectPath,
                        reason = "gradle.properties for project $projectDir not found"
                    )
                )
                return@forEach
            }

            commonProperties.keys.forEach { key ->
                if (!properties.containsKey(key)) {
                    inconsistencies.add(
                        Inconsistency(
                            projectPath = projectPath,
                            reason = "missing property: $key"
                        )
                    )
                } else {
                    val commonValue = commonProperties.getProperty(key)
                    val value = properties.getProperty(key)
                    if (value != commonValue) {
                        inconsistencies.add(
                            Inconsistency(
                                projectPath = projectPath,
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
                                projectPath,
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

            val map = inconsistencies.groupBy { it.projectPath }

            map.keys.forEach { project: String ->
                appendLine(" - project: $project")
                map[project]?.forEach { inconsistency: Inconsistency ->
                    appendLine("   - ${inconsistency.reason}")
                }
            }
        }
    }
}
