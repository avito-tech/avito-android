package com.avito.android.build_trace.internal

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

internal class CriticalPathSerialization(
    private val report: File
) {

    private val gson = GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .create()

    fun write(path: List<TaskOperation>) {
        ensureReportExists()
        report.writeText(gson.toJson(path))
    }

    fun read(): List<TaskOperation> {
        val type = object : TypeToken<List<TaskOperation>>() {}.type
        return gson.fromJson(report.bufferedReader(), type)
    }

    private fun ensureReportExists() {
        if (!report.exists()) {
            report.parentFile.mkdirs()
            report.createNewFile()
        }
    }
}
