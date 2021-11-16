package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.configuration.internalModule
import com.avito.kotlin.dsl.namedOrNull
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

public open class UnitTestCheck(context: String, name: String) : BuildStep(context, name),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        val allTestTask = project.tasks.register("${context}AllUnitTests") { allTestTask ->
            allTestTask.group = cdTaskGroup
            allTestTask.description = "Run all unit test in app module and all dependant modules"

            val configurations = project.internalModule.let {
                listOf(
                    it.testConfiguration,
                    it.mainConfiguration
                )
            }

            configurations.flatMap { it.allDependencies() }
                .toSet()
                .asSequence()
                .map { dependency -> dependency.module.project }
                .filter { dependency -> !useImpactAnalysis || dependency.internalModule.testConfiguration.isModified }
                .mapNotNull { dependency -> dependency.tasks.namedOrNull("test") }
                .forEach { moduleTestTask -> allTestTask.dependsOn(moduleTestTask) }
        }
        rootTask.dependsOn(allTestTask)
    }
}
