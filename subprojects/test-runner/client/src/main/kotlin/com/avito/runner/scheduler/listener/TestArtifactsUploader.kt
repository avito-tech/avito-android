package com.avito.runner.scheduler.listener

import com.avito.android.Result
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import java.io.File

internal interface TestArtifactsUploader {

    suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl>

    suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl>
}
