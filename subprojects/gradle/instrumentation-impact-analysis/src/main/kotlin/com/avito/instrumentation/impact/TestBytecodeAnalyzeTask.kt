package com.avito.instrumentation.impact

import com.avito.impact.BytecodeResolver
import com.avito.impact.ModifiedProjectsFinder
import com.avito.impact.util.RootId
import com.avito.impact.util.Screen
import com.avito.impact.util.Test
import com.avito.instrumentation.impact.model.AffectedTest
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

internal data class BytecodeAnalyzeSummary(
    val testsByScreen: Map<Screen, Set<Test>>,
    val testsAffectedByDependentOnUserChangedCode: Set<AffectedTest>,
    val testsModifiedByUser: Set<AffectedTest>,
    val rootIdByScreen: Map<Screen, RootId>
)

@Suppress("UnstableApiUsage")
abstract class TestBytecodeAnalyzeTask @Inject constructor(
    private val config: InstrumentationTestImpactAnalysisExtension,
    private val finder: ModifiedProjectsFinder,
    private val bytecodeResolver: BytecodeResolver,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @OutputFile
    val byteCodeAnalyzeSummary = config.output.file("bytecode-analyze-summary.json")

    @Suppress("unused")
    @TaskAction
    fun analyze() {
        workerExecutor.noIsolation().submit(TestBytecodeAnalyzeAction::class.java) { params ->
            params.state = TestBytecodeAnalyzeAction.Params.State(
                ciLogger = ciLogger,
                byteCodeAnalyzeSummary = byteCodeAnalyzeSummary,
                bytecodeResolver = bytecodeResolver,
                config = config,
                project = project,
                finder = finder
            )
        }
    }
}
