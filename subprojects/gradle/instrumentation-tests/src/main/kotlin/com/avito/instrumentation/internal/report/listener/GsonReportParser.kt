package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileReader

internal class GsonReportParser(private val gson: Gson = reportGson) : ReportParser {

    @Suppress("IfThenToElvis")
    override fun parse(reportFile: File): Result<TestRuntimeData> {
        return Result.tryCatch {
            val testRuntimeData: TestRuntimeData? = FileReader(reportFile).use {
                gson.fromJson(FileReader(reportFile), TestRuntimeDataPackage::class.java)
            }

            if (testRuntimeData == null) {
                throw IllegalStateException("Report file is empty: ${reportFile.path}")
            } else {
                testRuntimeData
            }
        }
    }

    companion object {

        internal val reportGson: Gson = GsonBuilder()
            .registerTypeAdapterFactory(EntryTypeAdapterFactory())
            .create()
    }
}
