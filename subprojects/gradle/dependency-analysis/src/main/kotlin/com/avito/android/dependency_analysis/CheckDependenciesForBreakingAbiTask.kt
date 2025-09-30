package com.avito.android.dependency_analysis

import com.autonomousapps.internal.utils.getJsonAdapter
import com.autonomousapps.model.ProjectAdvice
import okio.buffer
import okio.source
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/**
 * Check if the project has non-api dependencies that should be api.
 * A dependency should be api if the dependency classes are exposed in the project's ABI.
 */
@CacheableTask
internal abstract class CheckDependenciesForBreakingAbiTask : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val unfilteredProjectAdvice: RegularFileProperty

    @get:Optional
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val exclusions: RegularFileProperty

    @get:Optional
    @get:Input
    abstract val failureMessage: Property<String>

    @get:Input
    abstract val dependencyMap: MapProperty<String, String>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun check() {
        val advice: ProjectAdvice = unfilteredProjectAdvice.asFile.get().source().buffer().use {
            getJsonAdapter<ProjectAdvice>().fromJson(it)!!
        }
        val exclusions = exclusions.asFile.getOrNull()?.readLines().orEmpty()
        val failureMessage = failureMessage.getOrNull().orEmpty()
        val dependencyMap = dependencyMap.get()
        val outputFile = output.get().asFile

        val relevantAdvices = advice.dependencyAdvice.filter {
            it.isAnyChange() && it.isToApiLike() && it.coordinates.identifier !in exclusions
        }

        if (relevantAdvices.isNotEmpty()) {
            val errorText = buildString {
                appendLine("Existing dependencies which should be modified to be as indicated:")

                relevantAdvices.forEach {
                    append(it.toConfiguration)
                    append("(")
                    append(getPrintableIdentifier(dependencyMap, it.coordinates.identifier))
                    append(") (was ")
                    append(it.fromConfiguration)
                    append(")")
                    appendLine()
                }

                if (failureMessage.isNotBlank()) {
                    appendLine()
                    appendLine(failureMessage)
                }
            }

            outputFile.writeText(errorText)
            error(errorText)
        }

        outputFile.writeText("Success")
    }

    private fun getPrintableIdentifier(dependencyMap: Map<String, String>, identifier: String): String {
        return when {
            dependencyMap.containsKey(identifier) -> dependencyMap.getValue(identifier)
            identifier.startsWith(':') -> """project("$identifier")"""
            else -> """"$identifier""""
        }
    }
}
