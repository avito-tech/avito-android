package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.filestorage.RemoteStorage
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.io.File

internal class AvitoFileStorageUploader(
    private val remoteStorage: RemoteStorage
) : TestArtifactsUploader {

    override suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl> {
        return Result.tryCatch {
            when (type) {
                Entry.File.Type.html -> RemoteStorage.Request.ContentRequest.PlainText(content = content)
                Entry.File.Type.plain_text -> RemoteStorage.Request.ContentRequest.PlainText(content = content)
                Entry.File.Type.img_png,
                Entry.File.Type.video ->
                    throw IllegalArgumentException("Unsupported type $type ; direct file upload should be used")
            }
        }.flatMap { request ->
            when (val result = remoteStorage.upload(request, comment = "").get()) {
                is RemoteStorage.Result.Success -> Result.Success(remoteStorage.fullUrl(result).toHttpUrl())
                is RemoteStorage.Result.Error -> Result.Failure(result.t)
            }
        }
    }

    override suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl> {
        return Result.tryCatch {
            when (type) {
                Entry.File.Type.video -> RemoteStorage.Request.FileRequest.Video(file = file)
                Entry.File.Type.img_png -> RemoteStorage.Request.FileRequest.Image(file = file)

                // todo optimize it, upload directly from file stream
                Entry.File.Type.html -> RemoteStorage.Request.ContentRequest.Html(content = file.readText())
                Entry.File.Type.plain_text -> RemoteStorage.Request.ContentRequest.PlainText(content = file.readText())
            }
        }.flatMap { request ->
            when (val result = remoteStorage.upload(request, comment = "").get()) {
                is RemoteStorage.Result.Success -> Result.Success(remoteStorage.fullUrl(result).toHttpUrl())
                is RemoteStorage.Result.Error -> Result.Failure(result.t)
            }
        }
    }
}