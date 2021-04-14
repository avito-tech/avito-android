package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.model.AndroidTest
import com.avito.report.model.EntryTypeAdapterFactory
import com.avito.report.model.TestStaticData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

internal interface TestArtifactsProcessor {

    fun process(
        reportDir: File,
        testStaticData: TestStaticData,
        logcatBuffer: LogcatBuffer?
    ): Result<AndroidTest>

    companion object {

        internal val gson: Gson = GsonBuilder()
            .registerTypeAdapterFactory(EntryTypeAdapterFactory())
            .create()
    }
}
