@file:Suppress("UnstableApiUsage")

package com.avito.instrumentation.impact

import com.avito.android.isAndroid
import com.avito.impact.BytecodeResolver
import com.avito.impact.ModifiedProjectsFinder
import com.avito.utils.gradle.BuildEnvironment
import com.avito.utils.gradle.buildEnvironment
import com.avito.utils.gradle.isRoot
import com.google.gson.GsonBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import java.time.Duration

class InstrumentationTestImpactAnalysisPlugin : Plugin<Project> {

    private lateinit var modifiedProjectsFinder: ModifiedProjectsFinder
    private lateinit var modulesBytecodeResolver: BytecodeResolver

    override fun apply(project: Project) {
        require(project.isAndroid()) { "${this::class.java.simpleName} must be applied only in Android Application module type" }
        require(!project.isRoot()) { "${this::class.java.simpleName} should be applied to the specific project, which produces test apk" }
        require(project.rootProject.plugins.hasPlugin("com.avito.android.impact")) {
            "${this.javaClass.simpleName} requires impact plugin applied to the project"
        }

        val extension = project.extensions.create(
            "instrumentationTestImpactAnalysis",
            InstrumentationTestImpactAnalysisExtension::class.java,
            project
        )

        if (project.buildEnvironment !is BuildEnvironment.CI) return

        modifiedProjectsFinder = ModifiedProjectsFinder.from(project)
        modulesBytecodeResolver = BytecodeResolver(project)

        val bytecodeAnalyzeTask = project.tasks.register(
            "analyzeTestBytecode",
            TestBytecodeAnalyzeTask::class.java,
            extension,
            modifiedProjectsFinder,
            modulesBytecodeResolver
        )

        bytecodeAnalyzeTask.configure { task ->
            task.timeout.set(Duration.ofMinutes(20))
            task.group = TASK_GROUP
            task.description = "Analyze androidTest bytecode to collect maps: [Screen:Test], [Screen:RootId]"

            task.dependsOn("${project.path}:compileDebugAndroidTestKotlin") // TODO
        }
        project.tasks.register(
            TASK_ANALYZE_TEST_IMPACT_ANALYSIS,
            AnalyzeTestImpact::class.java,
            extension,
            modifiedProjectsFinder,
            gson
        ).configure { analyzeTask ->
            analyzeTask.group = TASK_GROUP
            analyzeTask.description = "Find tests to run based on changed modules and test code analysis"
            analyzeTask.dependsOn(bytecodeAnalyzeTask) // TODO https://docs.gradle.org/5.6.4/userguide/lazy_configuration.html#sec:working_with_task_dependencies_in_lazy_properties
            analyzeTask.apply {
                bytecodeAnalyzeSummaryJson.set(bytecodeAnalyzeTask.flatMap { it.byteCodeAnalyzeSummary })
            }
        }
    }

    companion object {
        val gson = GsonBuilder().setPrettyPrinting().create()
    }
}

open class InstrumentationTestImpactAnalysisExtension(project: Project) {
    val output = project.objects.directoryProperty()
    val screenMarkerClass = project.objects.property(String::class.java)
    val screenMarkerMetadataField = project.objects.property(String::class.java)
    val unknownRootId: Property<Int> = project.objects.property(Int::class.java).also { it.set(-1) }
    var packageFilter = project.objects.property(String::class.java)
}

private const val TASK_GROUP = "impact-analysis"
internal const val TASK_ANALYZE_TEST_IMPACT_ANALYSIS = "analyzeTestImpact"
