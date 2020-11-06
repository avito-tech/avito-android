@file:Suppress("UnstableApiUsage")

package com.avito.instrumentation.impact

import com.avito.android.isAndroidApp
import com.avito.android.withAndroidApp
import com.avito.impact.BytecodeResolver
import com.avito.impact.ModifiedProjectsFinder
import com.avito.kotlin.dsl.isRoot
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register
import java.time.Duration
import java.util.Locale

@ExperimentalStdlibApi
class InstrumentationTestImpactAnalysisPlugin : Plugin<Project> {

    private lateinit var modifiedProjectsFinder: ModifiedProjectsFinder
    private lateinit var modulesBytecodeResolver: BytecodeResolver

    override fun apply(project: Project) {
        checkPreconditions(project)

        val extension = project.extensions
            .create<InstrumentationTestImpactAnalysisExtension>("instrumentationTestImpactAnalysis", project)

        modifiedProjectsFinder = ModifiedProjectsFinder.from(project)
        modulesBytecodeResolver = BytecodeResolver(project)

        project.withAndroidApp {

            val bytecodeAnalyzeTask = project.tasks.register<TestBytecodeAnalyzeTask>(
                "analyzeTestBytecode",
                extension,
                modifiedProjectsFinder,
                modulesBytecodeResolver
            ) {
                timeout.set(Duration.ofMinutes(20))
                group = TASK_GROUP
                description = "Analyze androidTest bytecode to collect maps: [Screen:Test], [Screen:RootId]"

                val testBuildVariant = extension.testBuildVariant.convention(it.testBuildType)
                    .map { "${project.path}:compile${it.capitalize(Locale.getDefault())}AndroidTestKotlin" }

                dependsOn(testBuildVariant)
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
        }
    }

    private fun checkPreconditions(project: Project) {
        //todo should work with library tests as well
        require(project.isAndroidApp()) { "$PLUGIN_NAME must be applied only in Android Application module type" }

        require(!project.isRoot()) { "$PLUGIN_NAME should be applied to the specific project, which produces test apk" }

        require(project.rootProject.plugins.hasPlugin("com.avito.android.impact")) {
            "$PLUGIN_NAME requires `com.avito.android.impact` plugin applied to the root project"
        }
    }
}

private const val TASK_GROUP = "impact-analysis"
private const val PLUGIN_NAME = "InstrumentationTestImpactAnalysisPlugin"
internal const val TASK_ANALYZE_TEST_IMPACT_ANALYSIS = "analyzeTestImpact"
