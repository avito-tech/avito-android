package com.avito.android.plugin.build_metrics.internal.gradle.tasks.tech_budget

import com.avito.android.plugin.build_metrics.internal.BuildOperationsResult
import com.avito.android.plugin.build_metrics.internal.BuildOperationsResultListener
import com.avito.android.plugin.build_metrics.internal.TaskExecutionResult
import com.avito.android.plugin.build_metrics.internal.module
import com.avito.math.sumByLong
import com.avito.tech_budget.compilation_info.ModuleCompileTime
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import org.gradle.api.file.RegularFileProperty
import org.gradle.util.Path
import java.io.File
import java.lang.reflect.Type

internal class TechBudgetBuildTimeWriter(
    private val fileToWrite: RegularFileProperty,
) : BuildOperationsResultListener {

    override val name: String = "TechBudgetBuildTime"

    override fun onBuildFinished(result: BuildOperationsResult) {
        val data = computeTimePerModule(result.tasksExecutions)
        writeData(fileToWrite.get().asFile, data)
    }

    private fun computeTimePerModule(tasks: List<TaskExecutionResult>): List<ModuleCompileTime> {
        return tasks
            .groupBy { taskResult ->
                taskResult.path.module
            }
            .map { (path: Path, tasks: List<TaskExecutionResult>) ->
                val timeMs = tasks.sumByLong { it.elapsedMs }
                ModuleCompileTime(path.path, timeMs)
            }
    }

    private fun writeData(file: File, data: List<ModuleCompileTime>) {
        val type: Type = Types.newParameterizedType(List::class.java, ModuleCompileTime::class.java)
        val json = Moshi.Builder()
            .build()
            .adapter<List<ModuleCompileTime>>(type)
            .toJson(data)

        file.writeText(json)
    }
}
