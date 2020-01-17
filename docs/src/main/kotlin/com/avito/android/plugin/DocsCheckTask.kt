package com.avito.android.plugin

import com.avito.android.docker.Docker
import com.avito.utils.buildFailer
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class DocsCheckTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @InputDirectory
    val docsDirectory: DirectoryProperty = objects.directoryProperty()

    @TaskAction
    fun doWork() {
        val docker = Docker.fromProject(project)
        val buildFailer = project.buildFailer

        docker.build(docsDirectory.get().asFile).fold(
            { logger.lifecycle("Docs image tested: OK!") },
            { buildFailer.failBuild(it.message ?: "no message") }
        )
    }
}
