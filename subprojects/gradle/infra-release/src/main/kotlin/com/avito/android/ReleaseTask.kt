package com.avito.android

import com.avito.git.Git
import com.avito.git.gitState
import com.avito.utils.logging.ciLogger
import com.avito.utils.logging.commonLogger
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

abstract class ReleaseTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    @Input
    val releaseTag = objects.property<String>()

    @Input
    val previousReleaseTag = objects.property<String>()

    @TaskAction
    fun doWork() {
        val gitState = project.gitState { logger.lifecycle(it) }.get()
        val git: Git = Git.Impl(project.rootProject.rootDir) { logger.lifecycle(it) }

        val currentBranch = gitState.currentBranch.name

        Release(
            git = git,
            logger = commonLogger(ciLogger)
        ).release(
            releaseTag = releaseTag.get(),
            previousReleaseTag = previousReleaseTag.get(),
            currentBranch = currentBranch
        )
    }
}
