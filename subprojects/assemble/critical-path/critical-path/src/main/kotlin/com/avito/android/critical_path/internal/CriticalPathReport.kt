package com.avito.android.critical_path.internal

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

internal class CriticalPathReport(
    private val report: File
) {

    private val gson = GsonBuilder()
        .create()

    fun write(records: List<PathRecord>) {
        ensureReportExists()
        report.writeText(gson.toJson(records))
    }

    fun read(): List<PathRecord> {
        val type = object : TypeToken<List<PathRecord>>() {}.type
        return gson.fromJson(report.bufferedReader(), type)
    }

    private fun ensureReportExists() {
        if (!report.exists()) {
            report.parentFile.mkdirs()
            report.createNewFile()
        }
    }
}
