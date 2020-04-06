@file:Suppress("UnstableApiUsage")

package com.avito.instrumentation.impact

import com.avito.android.isAndroidApp
import com.avito.android.withAndroidApp
import com.avito.impact.BytecodeResolver
import com.avito.impact.ModifiedProjectsFinder
import com.avito.kotlin.dsl.isRoot
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.time.Duration

class InstrumentationTestImpactAnalysisPlugin : Plugin<Project> {

    private lateinit var modifiedProjectsFinder: ModifiedProjectsFinder
    private lateinit var modulesBytecodeResolver: BytecodeResolver

    override fun apply(project: Project) {
        checkPreconditions(project)

        val extension = project.extensions
            .create<InstrumentationTestImpactAnalysisExtension>("instrumentationTestImpactAnalysis", project)

        //todo should work in any environment
        if (project.buildEnvironment !is BuildEnvironment.CI) return

        modifiedProjectsFinder = ModifiedProjectsFinder.from(project)
        modulesBytecodeResolver = BytecodeResolver(project)

        project.withAndroidApp { app ->

            val testBuildTypeSlug = app.testBuildType.capitalize()

            val bytecodeAnalyzeTask = project.tasks.register<TestBytecodeAnalyzeTask>(
                "analyzeTestBytecode",
                extension,
                modifiedProjectsFinder,
                modulesBytecodeResolver
            ) {
                timeout.set(Duration.ofMinutes(20))
                group = TASK_GROUP
                description = "Analyze androidTest bytecode to collect maps: [Screen:Test], [Screen:RootId]"

                //todo we should also support flavors here
                dependsOn("${project.path}:compile${testBuildTypeSlug}AndroidTestKotlin")
            }

            project.tasks.register<AnalyzeTestImpactTask>(
                TASK_ANALYZE_TEST_IMPACT_ANALYSIS,
                extension,
                modifiedProjectsFinder
            ) {
                group = TASK_GROUP
                description = "Find tests to run based on changed modules and test code analysis"
                dependsOn(bytecodeAnalyzeTask)
                bytecodeAnalyzeSummaryJson.set(bytecodeAnalyzeTask.flatMap { it.byteCodeAnalyzeSummary })
            }

            val runtimeSymbolList = runtimeSymbolListPath(project.projectDir, app.testBuildType)
            val mergedAssets = mergedAssetsPath(project.projectDir, app.testBuildType)

            val copySymbolsToAssetsTask = project.tasks.register<Copy>("copy${testBuildTypeSlug}SymbolsToAssets") {
                from(runtimeSymbolList)
                //todo from all library modules
                into(mergedAssets)

                //todo onlyIf impactAnalysis will be performed

                //todo hook on android gradle plugin task outputs
                dependsOn("${project.path}:merge${testBuildTypeSlug}AndroidTestAssets")
                dependsOn("${project.path}:process${testBuildTypeSlug}Resources")
            }

            project.afterEvaluate {
                project.tasks.named("package${testBuildTypeSlug}AndroidTest").configure {
                    it.dependsOn(copySymbolsToAssetsTask)
                }
            }
        }
    }

    private fun checkPreconditions(project: Project) {
        //todo should work with library tests as well
        require(project.isAndroidApp()) { "$PLUGIN_NAME must be applied only in Android Application module type" }

        require(!project.isRoot()) { "$PLUGIN_NAME should be applied to the specific project, which produces test apk" }

        require(project.rootProject.plugins.hasPlugin("com.avito.android.impact")) {
            "$PLUGIN_NAME requires impact plugin applied to the project"
        }
    }
}

private const val TASK_GROUP = "impact-analysis"
private const val PLUGIN_NAME = "InstrumentationTestImpactAnalysisPlugin"
internal const val TASK_ANALYZE_TEST_IMPACT_ANALYSIS = "analyzeTestImpact"
