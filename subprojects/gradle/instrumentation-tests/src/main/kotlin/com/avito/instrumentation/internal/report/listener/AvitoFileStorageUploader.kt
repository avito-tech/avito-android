package com.avito.instrumentation.internal.report.listener

import com.avito.android.Result
import com.avito.filestorage.RemoteStorage
import com.avito.filestorage.RemoteStorageRequest
import com.avito.report.model.Entry
import okhttp3.HttpUrl
import java.io.File

internal class AvitoFileStorageUploader(
    private val remoteStorage: RemoteStorage
) : TestArtifactsUploader {

    override suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl> {
        return Result.tryCatch {
            when (type) {
                Entry.File.Type.html -> RemoteStorageRequest.ContentRequest.PlainText(content = content)
                Entry.File.Type.plain_text -> RemoteStorageRequest.ContentRequest.PlainText(content = content)
                Entry.File.Type.img_png,
                Entry.File.Type.video ->
                    throw IllegalArgumentException("Unsupported type $type ; direct file upload should be used")
            }
        }.flatMap { request ->
            remoteStorage.upload(request).get()
        }
    }

    override suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl> {
        return Result.tryCatch {
            when (type) {
                Entry.File.Type.video -> RemoteStorageRequest.FileRequest.Video(file = file)
                Entry.File.Type.img_png -> RemoteStorageRequest.FileRequest.Image(file = file)

                // todo optimize it, upload directly from file stream
                Entry.File.Type.html -> RemoteStorageRequest.ContentRequest.Html(content = file.readText())
                Entry.File.Type.plain_text -> RemoteStorageRequest.ContentRequest.PlainText(content = file.readText())
            }
        }.flatMap { request ->
            remoteStorage.upload(request).get()
        }
    }
}
