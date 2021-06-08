package com.avito.report.serialize

import com.avito.android.Result
import com.avito.report.model.FileAddress
import com.avito.report.model.TestRuntimeData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

public interface ReportSerializer {

    public fun serialize(testRuntimeData: TestRuntimeData, reportFile: File): Result<File>

    public fun deserialize(reportFile: File): Result<TestRuntimeData>
}

public fun ReportSerializer(): ReportSerializer = GsonReportSerializer(gson = reportGson)

internal val reportGson: Gson = GsonBuilder()
    .registerTypeAdapterFactory(EntryTypeAdapterFactory())
    .registerTypeAdapter(FileAddress::class.java, FileAddressTypeAdapter())
    .create()
