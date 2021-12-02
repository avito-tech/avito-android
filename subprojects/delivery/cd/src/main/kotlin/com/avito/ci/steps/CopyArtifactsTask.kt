package com.avito.ci.steps

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import java.io.File

public abstract class CopyArtifactsTask : DefaultTask() {

    @Input
    public val sourceDir: DirectoryProperty = project.objects.directoryProperty()

    @Input
    public val destinationDir: DirectoryProperty = project.objects.directoryProperty()

    @InputFiles
    public val entries: Property<FileCollection> = project.objects.property()

    @TaskAction
    public fun doAction() {

        entries.get().forEach { entry ->
            if (entry.exists()) {
                val relativeEntryPath = entry.relativeTo(sourceDir.get().asFile).path
                val destination = File(destinationDir.get().asFile, relativeEntryPath)
                logger.debug("Copying ${entry.path} to $destination")
                entry.copyTo(destination, overwrite = true) // TODO: убрать перезапись после MBS-5491
            } else {
                logger.info("Can't copy ${entry.path} it does not exist")
            }
        }
    }
}
