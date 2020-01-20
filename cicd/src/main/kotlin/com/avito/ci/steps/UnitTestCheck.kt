package com.avito.ci.steps

import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.avito.impact.configuration.internalModule
import com.avito.kotlin.dsl.namedOrNull
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

open class UnitTestCheck(context: String) : BuildStep(context),
    ImpactAnalysisAwareBuildStep by ImpactAnalysisAwareBuildStep.Impl() {

    override fun registerTask(project: Project, rootTask: TaskProvider<out Task>) {
        val allTestTask = project.tasks.register("${context}AllUnitTests") { allTestTask ->
            allTestTask.group = "cd"
            allTestTask.description = "Run all unit test in app module and all dependant modules"

            val configurations = project.internalModule.let {
                listOf(
                    it.androidTestConfiguration,
                    it.testConfiguration,
                    it.implementationConfiguration
                )
            }

            configurations.flatMap { it.allDependencies() }
                .toSet()
                .asSequence()
                .map { dependency -> dependency.module.project }
                .filter { dependency -> !useImpactAnalysis || dependency.internalModule.implementationConfiguration.isModified }
                .mapNotNull { dependency -> dependency.tasks.namedOrNull("test") }
                .forEach { moduleTestTask -> allTestTask.dependsOn(moduleTestTask) }
        }
        rootTask.dependsOn(allTestTask)
    }

}

