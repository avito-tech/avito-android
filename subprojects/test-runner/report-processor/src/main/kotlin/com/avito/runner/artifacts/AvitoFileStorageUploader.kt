package com.avito.runner.artifacts

import com.avito.android.Result
import com.avito.filestorage.ContentType
import com.avito.filestorage.RemoteStorage
import com.avito.report.model.Entry
import kotlinx.coroutines.delay
import okhttp3.HttpUrl
import java.io.File
import java.util.concurrent.TimeUnit

internal class AvitoFileStorageUploader(
    private val remoteStorage: RemoteStorage,
    private val delayTimeMs: Long = TimeUnit.MILLISECONDS.toMillis(500)
) : TestArtifactsUploader {

    override suspend fun upload(content: String, type: Entry.File.Type): Result<HttpUrl> {
        val uploadFuture = remoteStorage.upload(content, type.toContentType())
        var result: Result<HttpUrl>? = uploadFuture.tryGet()
        while (result == null) {
            delay(delayTimeMs)
            result = uploadFuture.tryGet()
        }
        return result
    }

    override suspend fun upload(file: File, type: Entry.File.Type): Result<HttpUrl> {
        val uploadFuture = remoteStorage.upload(file = file, type = type.toContentType())
        var result: Result<HttpUrl>? = uploadFuture.tryGet()
        while (result == null) {
            delay(delayTimeMs)
            result = uploadFuture.tryGet()
        }
        return result
    }

    private fun Entry.File.Type.toContentType(): ContentType {
        return when (this) {
            Entry.File.Type.html -> ContentType.HTML
            Entry.File.Type.img_png -> ContentType.PNG
            Entry.File.Type.video -> ContentType.MP4
            Entry.File.Type.plain_text -> ContentType.TXT
        }
    }
}
