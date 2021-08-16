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

abstract class GenerateCommonProperties : DefaultTask() {

    @get:InputFile
    abstract val commonPropertiesFile: RegularFileProperty

    @get:OutputDirectories
    abstract val projectDirs: SetProperty<Directory>

    @TaskAction
    fun doWork() {
        val commonProperties = PropertiesConfiguration(commonPropertiesFile.get().asFile)

        projectDirs.get().forEach { projectDir ->

            val gradlePropertiesFile = File(projectDir.asFile, "gradle.properties")

            val properties = if (gradlePropertiesFile.exists()) {
                PropertiesConfiguration(gradlePropertiesFile)
            } else {
                PropertiesConfiguration(gradlePropertiesFile)
            }

            commonProperties.keys.forEach { key ->
                properties.setProperty(key, commonProperties.getProperty(key))
                val commonComment: String? = commonProperties.getRawComment(key)
                val comment = generateComment(commonComment)
                properties.layout.setComment(key, comment)
            }

            properties.header = buildString {
                appendLine("Has common auto generated part!")
                appendLine("Modify it at: `conf/common.gradle.properties`")
                append("then use :generateCommonProperties task")
            }

            properties.layout.globalSeparator = "="

            properties.save()
        }
    }
}
