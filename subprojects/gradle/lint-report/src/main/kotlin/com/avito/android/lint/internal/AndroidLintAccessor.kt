package com.avito.android.lint.internal

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.UnknownTaskException
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * todo find outputs for direct dependency
 *
 * todo support build variants
 */
class AndroidLintAccessor(private val project: Project) {

    fun taskProvider(): TaskProvider<Task> = try {
        project.tasks.named("lintRelease")
    } catch (e: UnknownTaskException) {
        // local android sync usually doesn't have release variant enabled
        project.tasks.named("lint")
    }

    fun resultXml(): File = File("${project.buildDir}/reports/lint-results-release.xml")

    fun resultHtml(): File = File("${project.buildDir}/reports/lint-results-release.html")
}
