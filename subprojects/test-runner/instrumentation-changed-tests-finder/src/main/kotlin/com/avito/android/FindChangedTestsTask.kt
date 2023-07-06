package com.avito.android

import com.avito.gradle.worker.inMemoryWork
import com.avito.logger.GradleLoggerPlugin
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

/**
 * todo @CacheableTask + test for it
 */
public abstract class FindChangedTestsTask @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @Input
    public val targetCommit: Property<String> = objects.property()

    @InputDirectory
    public val androidTestDir: DirectoryProperty = objects.directoryProperty()
        .convention(layout.projectDirectory.dir("src/androidTest"))

    @OutputFile
    public val changedTestsFile: Provider<RegularFile> = objects.directoryProperty()
        .convention(layout.buildDirectory)
        .file("changed-test-classes.txt")

    @TaskAction
    public fun doWork() {
        project.rootProject.layout.projectDirectory
        workerExecutor.inMemoryWork {
            FindChangedTestsAction(
                project.rootProject.layout.projectDirectory,
                targetCommit,
                androidTestDir,
                changedTestsFile.get(),
                GradleLoggerPlugin.getLoggerFactory(this)
            ).execute()
        }
    }
}
