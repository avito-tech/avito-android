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

    override suspend fun uploadLogcat(logcat: String): Result<HttpUrl> {
        return Result.tryCatch {
            val uploadResult = remoteStorage.upload(
                RemoteStorage.Request.ContentRequest.AnyContent(
                    content = logcat,
                    extension = "log"
                ),
                comment = "logcat"
            ).get()

            when (uploadResult) {
                is RemoteStorage.Result.Success -> remoteStorage.fullUrl(uploadResult).toHttpUrl()
                is RemoteStorage.Result.Error -> throw uploadResult.t
            }
        }
    }

    override suspend fun uploadFile(file: File, type: Entry.File.Type): Result<HttpUrl> {
        return Result.tryCatch {
            when (type) {
                Entry.File.Type.video -> RemoteStorage.Request.FileRequest.Video(file = file)
                Entry.File.Type.img_png -> RemoteStorage.Request.FileRequest.Image(file = file)
                else -> throw IllegalArgumentException("MediaType:$type not supported by AvitoFileStorage")
            }
        }.flatMap { request ->
            when (val result = remoteStorage.upload(request, comment = "").get()) {
                is RemoteStorage.Result.Success -> Result.Success(remoteStorage.fullUrl(result).toHttpUrl())
                is RemoteStorage.Result.Error -> Result.Failure(result.t)
            }
        }
    }
}
