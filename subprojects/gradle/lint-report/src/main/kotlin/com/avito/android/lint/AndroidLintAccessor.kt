package com.avito.android.lint

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import java.io.File

/**
 * todo find outputs for direct dependency
 *
 * todo support build variants
 */
class AndroidLintAccessor(private val project: Project) {

    fun taskProvider(): TaskProvider<Task> = project.tasks.named("lintRelease")

    fun resultXml(): File = File("${project.buildDir}/reports/lint-results-release.xml")

    fun resultHtml(): File = File("${project.buildDir}/reports/lint-results-release.html")
}
