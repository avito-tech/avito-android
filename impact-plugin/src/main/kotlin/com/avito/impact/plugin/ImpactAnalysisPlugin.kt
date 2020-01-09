package com.avito.impact.plugin

import com.avito.git.GitState
import com.avito.git.gitState
import com.avito.impact.changes.newChangesDetector
import com.avito.impact.configuration.InternalModule
import com.avito.impact.configuration.internalModule
import com.avito.impact.impactFallbackDetector
import com.avito.utils.gradle.isRoot
import com.avito.utils.logging.ciLogger
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register

class ImpactAnalysisPlugin : Plugin<Project> {

    override fun apply(rootProject: Project) {
        val pluginName = ImpactAnalysisPlugin::class.java.simpleName

        check(rootProject.isRoot()) { "Plugin $pluginName should be applied to the root project" }

        rootProject.afterEvaluate {
            check(rootProject.subprojects.size > 0) { "Plugin $pluginName does not support single root-module projects" }
        }

        rootProject.extensions.create("impactAnalysis", ImpactAnalysisExtension::class.java)

        val gitState: GitState? = rootProject.gitState { rootProject.ciLogger.info(it) }.orNull

        val changesDetector = newChangesDetector(
            rootDir = rootProject.rootDir,
            targetCommit = gitState?.targetBranch?.commit,
            logger = rootProject.ciLogger
        )

        rootProject.subprojects.forEach { subProject ->
            subProject.internalModule = InternalModule(
                project = subProject,
                changesDetector = changesDetector,
                fallbackDetector = subProject.impactFallbackDetector
            )
        }
        registerModulesReport(rootProject)
    }

    private fun registerModulesReport(project: Project) {
        project.tasks.register<GenerateModulesReport>("generateModulesReport") {
            group = "impact-analysis"
            description = "Print modified projects to files. For testing purposes only"
        }
    }
}
