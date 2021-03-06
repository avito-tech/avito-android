package com.avito.runner.artifacts

import com.avito.android.Result
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File

internal class StubTestArtifactsUploader : TestArtifactsUploader {

    override suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl> {
        return Result.Success("https://stub".toHttpUrl())
    }

    override suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl> {
        return Result.Success("https://stub".toHttpUrl())
    }
}
