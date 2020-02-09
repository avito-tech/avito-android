package com.avito.instrumentation.impact

import com.avito.impact.BytecodeResolver
import com.avito.impact.ModifiedProjectsFinder
import com.avito.utils.logging.ciLogger
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject

@Suppress("UnstableApiUsage")
abstract class TestBytecodeAnalyzeTask @Inject constructor(
    private val config: InstrumentationTestImpactAnalysisExtension,
    private val finder: ModifiedProjectsFinder,
    private val bytecodeResolver: BytecodeResolver,
    private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @OutputFile
    val byteCodeAnalyzeSummary: Provider<RegularFile> = config.output.file("bytecode-analyze-summary.json")

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
