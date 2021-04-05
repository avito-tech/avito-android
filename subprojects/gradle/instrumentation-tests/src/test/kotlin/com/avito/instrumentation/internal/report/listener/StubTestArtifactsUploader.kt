package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File

internal class StubTestArtifactsUploader : TestArtifactsUploader {

    override suspend fun uploadFile(file: File, type: Entry.File.Type): Result<HttpUrl> {
        return Result.Success("http://stub".toHttpUrl())
    }

    override suspend fun uploadLogcat(logcat: String): Result<HttpUrl> {
        return Result.Success("http://stub".toHttpUrl())
    }
}
