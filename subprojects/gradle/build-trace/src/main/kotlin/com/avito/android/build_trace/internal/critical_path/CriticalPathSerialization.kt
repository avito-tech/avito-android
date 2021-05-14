package com.avito.android.build_trace.internal.critical_path

import com.avito.graph.OperationsPath
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

internal class CriticalPathSerialization(
    private val report: File
) {

    private val gson = GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .create()

    fun write(path: OperationsPath<TaskOperation>) {
        ensureReportExists()
        report.writeText(gson.toJson(path.operations))
    }

    fun read(): OperationsPath<TaskOperation> {
        val type = object : TypeToken<List<TaskOperation>>() {}.type
        val operations: List<TaskOperation> = gson.fromJson(report.bufferedReader(), type)
        return OperationsPath(operations)
    }

    private fun ensureReportExists() {
        if (!report.exists()) {
            report.parentFile.mkdirs()
            report.createNewFile()
        }
    }
}
