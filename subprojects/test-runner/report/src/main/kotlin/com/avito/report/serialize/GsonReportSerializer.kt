package com.avito.report.serialize

import com.avito.android.Result
import com.avito.report.model.TestRuntimeData
import com.avito.report.model.TestRuntimeDataPackage
import com.google.gson.Gson
import java.io.File
import java.io.FileReader

internal class GsonReportSerializer(private val gson: Gson) : ReportSerializer {

    override fun serialize(testRuntimeData: TestRuntimeData, reportFile: File): Result<File> {
        return Result.tryCatch {
            val json = gson.toJson(testRuntimeData)
            reportFile.writeText(json)
            reportFile
        }
    }

    @Suppress("IfThenToElvis")
    override fun deserialize(reportFile: File): Result<TestRuntimeData> {
        return Result.tryCatch {
            val testRuntimeData: TestRuntimeData? = FileReader(reportFile).use { reader ->
                gson.fromJson(reader, TestRuntimeDataPackage::class.java)
            }

            if (testRuntimeData == null) {
                throw IllegalStateException("Report file is empty: ${reportFile.path}")
            } else {
                testRuntimeData
            }
        }
    }
}
