@file:Suppress("UnstableApiUsage")

package com.avito.kotlin.dsl

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import java.io.File
import javax.inject.Inject

/**
 * Plugin for testing purposes.
 * We can't move it to the testFixtures because of gradle plugin publishing limitations.
 */
class TestPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val providerTask = target.tasks.register<ProviderTask>("provider") {

            outputFile.set(File("nonexistent.file"))
        }

        target.tasks.register<ConsumerTask>("consumer") {

            dependencyOn(providerTask) {
                if (target.getBooleanProperty("useSkipIfNotExists")) {
                    inputFile.set(it.outputFile.optionalIfNotExists())
                } else {
                    inputFile.set(it.outputFile)
                }
            }
        }
    }
}

abstract class ProviderTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @OutputFile
    val outputFile: RegularFileProperty = objects.fileProperty()
}

abstract class ConsumerTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @InputFile
    @Optional
    val inputFile: RegularFileProperty = objects.fileProperty()

    @TaskAction
    fun doWork() {
        println(inputFile.asFile.orNull)
    }
}
