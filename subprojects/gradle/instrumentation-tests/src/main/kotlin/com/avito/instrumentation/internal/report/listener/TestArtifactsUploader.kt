package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import java.io.File

internal interface TestArtifactsUploader {

    suspend fun uploadLogcat(logcat: String): Result<HttpUrl>

    suspend fun uploadFile(file: File, type: Entry.File.Type): Result<HttpUrl>
}
