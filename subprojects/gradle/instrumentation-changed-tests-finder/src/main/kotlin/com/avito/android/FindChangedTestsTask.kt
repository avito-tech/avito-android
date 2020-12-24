package com.avito.android

import com.avito.logger.GradleLoggerFactory
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.model.ObjectFactory
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
@Suppress("UnstableApiUsage")
abstract class FindChangedTestsTask @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @Input
    val targetCommit = objects.property<String>()

    @InputDirectory
    val androidTestDir: DirectoryProperty = objects.directoryProperty()
        .convention(layout.projectDirectory.dir("src/androidTest"))

    @OutputFile
    val changedTestsFile: Provider<RegularFile> = objects.directoryProperty()
        .convention(layout.buildDirectory)
        .file("changed-test-classes.txt")

    @TaskAction
    fun doWork() {
        val loggerFactory = GradleLoggerFactory.fromTask(this)

        workerExecutor.noIsolation().submit(FindChangedTestsAction::class.java) { params ->
            params.rootDir.set(project.rootDir)
            params.targetCommit.set(targetCommit)
            params.loggerFactory.set(loggerFactory)
            params.androidTestDir.set(androidTestDir)
            params.changedTestsFile.set(changedTestsFile)
        }
    }
}
